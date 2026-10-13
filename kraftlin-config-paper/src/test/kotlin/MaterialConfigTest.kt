package io.github.kraftlin.config.paper

import io.github.kraftlin.config.AbstractConfig
import io.github.kraftlin.config.wrapConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.entity.EntityType
import org.junit.jupiter.api.io.TempDir
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class MaterialConfigTest {

    @TempDir
    lateinit var directory: Path

    private val configFile: Path
        get() = directory.resolve("config.yml")

    @BeforeTest
    fun mockTagRegistry() {
        mockkStatic(Bukkit::class)
        every { Bukkit.getTag(any(), any(), any<Class<Keyed>>()) } returns null
        every { Bukkit.getTag(Tag.REGISTRY_BLOCKS, MINEABLE_PICKAXE_KEY, Material::class.java) } returns MINEABLE_PICKAXE
        every { Bukkit.getTag(Tag.REGISTRY_ITEMS, ENCHANTABLE_SWORD_KEY, Material::class.java) } returns ENCHANTABLE_SWORD
    }

    @AfterTest
    fun unmockTagRegistry() {
        unmockkStatic(Bukkit::class)
    }

    @Test
    fun `material collections resolve to a material set and other enum lists to enum lists`() {
        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {
            val list = config("list", listOf(Material.STONE))
            val listWithComments = config("listWithComments", listOf(Material.STONE), "comment")
            val set = config("set", setOf(Material.STONE))
            val setWithComments = config("setWithComments", setOf(Material.STONE), "comment")
            val empty = config("empty", emptyList<Material>())
            val emptyWithComments = config("emptyWithComments", emptyList<Material>(), "comment")
            val materialSet = config("materialSet", MaterialSet(tags = listOf(MINEABLE_PICKAXE)))
            val materialSetWithComments = config("materialSetWithComments", materialSetOf(Material.STONE), "comment")
            val materialList = config<Material>("materialList", listOf(Material.STONE))
            val materialListWithComments = config<Material>("materialListWithComments", listOf(Material.STONE), "comment")
            val entities = config("entities", listOf(EntityType.ZOMBIE))
            val entitiesWithComments = config("entitiesWithComments", listOf(EntityType.ZOMBIE), "comment")
        }

        // These declarations only compile if each call above resolved to the overload named by the type
        val materialSets: List<AbstractConfig.ConfigDelegate<MaterialSet>> = listOf(
            config.list, config.listWithComments, config.set, config.setWithComments, config.empty,
            config.emptyWithComments, config.materialSet, config.materialSetWithComments
        )
        val materialLists: List<AbstractConfig.ConfigDelegate<List<Material>>> =
            listOf(config.materialList, config.materialListWithComments)
        val entityLists: List<AbstractConfig.ConfigDelegate<List<EntityType>>> =
            listOf(config.entities, config.entitiesWithComments)

        config.saveDefaults()

        assertEquals(8, materialSets.size)
        assertEquals(2, materialLists.size)
        assertEquals(2, entityLists.size)
        assertEquals(listOf("minecraft:stone"), storedList("list"))
        assertEquals(listOf("#minecraft:mineable/pickaxe"), storedList("materialSet"))
        assertEquals(listOf("stone"), storedList("materialList"))
        assertEquals(listOf("zombie"), storedList("entities"))
    }

    @Test
    fun `other enum lists are read as enum lists with and without comments`() {
        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {
            val mobs: List<EntityType> by config("mobs", listOf(EntityType.ZOMBIE))
            val commentedMobs: List<EntityType> by config("commentedMobs", listOf(EntityType.ZOMBIE), "Mobs to spawn")
        }

        config.saveDefaults()

        assertEquals(listOf(EntityType.ZOMBIE), config.mobs)
        assertEquals(listOf(EntityType.ZOMBIE), config.commentedMobs)
        assertTrue(Files.readString(configFile).contains("# Mobs to spawn"), "Comment should be written")
    }

    @Test
    fun `empty material list reads as empty set`() {
        Files.writeString(configFile, "blocks: []\n")

        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {
            val blocks: MaterialSet by config("blocks", listOf(Material.STONE))
        }

        assertEquals(emptySet(), config.blocks)
        assertEquals(emptySet(), config.blocks.tags)
    }

    @Test
    fun `materials and tags are read with or without namespace`() {
        Files.writeString(
            configFile,
            "blocks:\n- minecraft:stone\n- dirt\n- '#minecraft:mineable/pickaxe'\n- '#mineable/pickaxe'\n"
        )
        val warnings = RecordingHandler()

        val config = object : AbstractBukkitConfig(wrapConfig(configFile, loggerWith(warnings))) {
            val blocks: MaterialSet by config("blocks", listOf(Material.SAND))
        }

        assertEquals(setOf(Material.STONE, Material.DIRT), config.blocks.materials)
        assertEquals(setOf(MINEABLE_PICKAXE), config.blocks.tags)
        assertEquals(emptyList(), warnings.messages)
    }

    @Test
    fun `unknown material names are skipped with a warning`() {
        Files.writeString(configFile, "blocks:\n- stone\n- not_a_material\n")
        val warnings = RecordingHandler()

        val config = object : AbstractBukkitConfig(wrapConfig(configFile, loggerWith(warnings))) {
            val blocks: MaterialSet by config("blocks", listOf(Material.DIRT))
        }

        assertEquals(setOf(Material.STONE), config.blocks)
        assertEquals(1, warnings.messages.size)
        assertTrue(warnings.messages.single().contains("blocks"), "Warning should name the path")
        assertTrue(warnings.messages.single().contains("not_a_material"), "Warning should name the unknown entry")
    }

    @Test
    fun `only unknown material names read as empty set`() {
        Files.writeString(configFile, "blocks:\n- not_a_material\n")

        val config = object : AbstractBukkitConfig(wrapConfig(configFile, loggerWith(RecordingHandler()))) {
            val blocks: MaterialSet by config("blocks", listOf(Material.STONE))
        }

        assertEquals(emptySet(), config.blocks)
    }

    @Test
    fun `legacy materials are skipped with a warning`() {
        Files.writeString(configFile, "blocks:\n- stone\n- legacy_stone\n")
        val warnings = RecordingHandler()

        val config = object : AbstractBukkitConfig(wrapConfig(configFile, loggerWith(warnings))) {
            val blocks: MaterialSet by config("blocks", listOf(Material.DIRT))
        }

        assertEquals(setOf(Material.STONE), config.blocks)
        assertEquals(1, warnings.messages.size)
        assertTrue(warnings.messages.single().contains("blocks"), "Warning should name the path")
        assertTrue(warnings.messages.single().contains("legacy_stone"), "Warning should name the legacy entry")
    }

    @Test
    fun `material list comments are written`() {
        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {
            val blocks: MaterialSet by config("blocks", listOf(Material.STONE), "Blocks the pet may dig")
        }

        config.saveDefaults()

        val content = Files.readString(configFile)
        assertTrue(content.contains("# Blocks the pet may dig"), "Comment should be written for the material list")
        assertEquals(setOf(Material.STONE), config.blocks)
    }

    @Test
    fun `defaults are written sorted with the tags of a material set`() {
        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {
            val blocks: MaterialSet by config(
                "blocks",
                MaterialSet(listOf(Material.STONE, Material.DIRT), listOf(MINEABLE_PICKAXE))
            )
            val plain: MaterialSet by config("plain", listOf(Material.STONE, Material.DIRT, Material.STONE))
        }

        config.saveDefaults()

        assertEquals(listOf("#minecraft:mineable/pickaxe", "minecraft:dirt", "minecraft:stone"), storedList("blocks"))
        assertEquals(listOf("minecraft:dirt", "minecraft:stone"), storedList("plain"))
        assertEquals(setOf(MINEABLE_PICKAXE), config.blocks.tags)
    }

    @Test
    fun `tags with a slash in their key are resolved`() {
        val warnings = RecordingHandler()

        val config = object : AbstractBukkitConfig(wrapConfig(configFile, loggerWith(warnings))) {
            val blocks: MaterialSet by config("blocks", MaterialSet(tags = listOf(MINEABLE_PICKAXE)))
        }

        config.saveDefaults()

        assertEquals(listOf("#minecraft:mineable/pickaxe"), storedList("blocks"))
        assertEquals(MINEABLE_PICKAXE.values, config.blocks)
        assertEquals(emptyList(), warnings.messages)
    }

    @Test
    fun `tags with a slash in their key are resolved without namespace`() {
        Files.writeString(configFile, "blocks:\n- '#mineable/pickaxe'\n")
        val warnings = RecordingHandler()

        val config = object : AbstractBukkitConfig(wrapConfig(configFile, loggerWith(warnings))) {
            val blocks: MaterialSet by config("blocks", listOf(Material.DIRT))
        }

        assertEquals(MINEABLE_PICKAXE.values, config.blocks)
        assertEquals(setOf(MINEABLE_PICKAXE), config.blocks.tags)
        assertEquals(emptyList(), warnings.messages)
    }

    @Test
    fun `item tags with a slash in their key are resolved`() {
        Files.writeString(configFile, "items:\n- '#minecraft:enchantable/sword'\n")
        val warnings = RecordingHandler()

        val config = object : AbstractBukkitConfig(wrapConfig(configFile, loggerWith(warnings))) {
            val items: MaterialSet by config("items", listOf(Material.DIRT))
        }

        assertEquals(ENCHANTABLE_SWORD.values, config.items)
        assertEquals(setOf(ENCHANTABLE_SWORD), config.items.tags)
        assertEquals(emptyList(), warnings.messages)
    }

    @Test
    fun `tags with a slash in their namespace are skipped with a warning`() {
        Files.writeString(configFile, "blocks:\n- '#mine/able:pickaxe'\n")
        val warnings = RecordingHandler()

        val config = object : AbstractBukkitConfig(wrapConfig(configFile, loggerWith(warnings))) {
            val blocks: MaterialSet by config("blocks", listOf(Material.STONE))
        }

        assertEquals(emptySet(), config.blocks)
        assertEquals(1, warnings.messages.size)
        assertTrue(warnings.messages.single().contains("#mine/able:pickaxe"), "Warning should name the invalid entry")
    }

    @Test
    fun `tags are kept as tags when read and written back`() {
        Files.writeString(configFile, "blocks:\n- stone\n- '#mineable/pickaxe'\n")

        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {
            var blocks: MaterialSet by config("blocks", listOf(Material.SAND))
        }

        assertEquals(setOf(Material.STONE), config.blocks.materials)
        assertEquals(setOf(MINEABLE_PICKAXE), config.blocks.tags)
        assertEquals(setOf(Material.STONE, Material.IRON_ORE), config.blocks)

        config.blocks += Material.DIRT
        config.save()

        assertEquals(listOf("#minecraft:mineable/pickaxe", "minecraft:dirt", "minecraft:stone"), storedList("blocks"))
    }

    @Test
    fun `setting a material set keeps its tags in the file`() {
        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {
            var blocks: MaterialSet by config("blocks", listOf(Material.STONE))
        }
        config.saveDefaults()

        config.blocks = MaterialSet(listOf(Material.DIRT), listOf(MINEABLE_PICKAXE))
        config.save()

        assertEquals(listOf("#minecraft:mineable/pickaxe", "minecraft:dirt"), storedList("blocks"))
        assertEquals(setOf(Material.DIRT), config.blocks.materials)
        assertEquals(setOf(MINEABLE_PICKAXE), config.blocks.tags)
    }

    @Test
    fun `reloading resolves tags again`() {
        val growingKey = NamespacedKey.minecraft("growing")
        var members = setOf(Material.OAK_LOG)
        val growing: Tag<Material> = mockk {
            every { key } returns growingKey
            every { values } answers { members }
        }
        every { Bukkit.getTag(Tag.REGISTRY_BLOCKS, growingKey, Material::class.java) } returns growing
        Files.writeString(configFile, "blocks:\n- '#growing'\n")

        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {
            val blocks: MaterialSet by config("blocks", listOf(Material.DIRT))
        }
        assertEquals(setOf(Material.OAK_LOG), config.blocks)

        members = setOf(Material.OAK_LOG, Material.CHERRY_LOG)
        assertEquals(setOf(Material.OAK_LOG), config.blocks, "The value read before stays unchanged")
        config.reloadConfig()

        assertEquals(setOf(Material.OAK_LOG, Material.CHERRY_LOG), config.blocks)
        assertEquals(setOf(growing), config.blocks.tags)
    }

    @Test
    fun `plugins compiled against 0_8_0 can still create material sets`() {
        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {}
        // config(path, default: Collection<Keyed>) as compiled in 0.8.0, now hidden from Kotlin sources
        val method = AbstractBukkitConfig::class.java.getDeclaredMethod(
            "configSetMaterial",
            String::class.java,
            Collection::class.java
        )
        assertTrue(Modifier.isProtected(method.modifiers), "The method should stay protected")
        assertEquals(AbstractConfig.ConfigDelegate::class.java, method.returnType)
        method.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val delegate = method.invoke(config, "blocks", listOf(Material.DIRT, MINEABLE_PICKAXE))
            as AbstractConfig.ConfigDelegate<Set<Material>>
        var blocks: Set<Material> by delegate
        config.saveDefaults()

        assertEquals(listOf("#minecraft:mineable/pickaxe", "minecraft:dirt"), storedList("blocks"))
        assertEquals(setOf(Material.DIRT, Material.STONE, Material.IRON_ORE), blocks)

        blocks = setOf(Material.SAND)
        config.save()

        assertEquals(listOf("minecraft:sand"), storedList("blocks"))
        assertEquals(setOf(Material.SAND), blocks)
    }

    @Test
    fun `the 0_8_0 material set overload rejects defaults other than materials and material tags`() {
        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {}
        val method = AbstractBukkitConfig::class.java.getDeclaredMethod(
            "configSetMaterial",
            String::class.java,
            Collection::class.java
        )
        method.isAccessible = true
        val entityTag: Tag<EntityType> = mockk {
            every { key } returns NamespacedKey.minecraft("undead")
            every { values } returns setOf(EntityType.ZOMBIE)
        }

        for (default in listOf(listOf(EntityType.ZOMBIE), listOf(entityTag))) {
            val exception = assertFailsWith<InvocationTargetException> { method.invoke(config, "mobs", default) }
            assertIs<IllegalArgumentException>(exception.cause)
        }
    }

    /** The raw string list stored at [path] in the config file. */
    private fun storedList(path: String): List<String> {
        val stored = object : AbstractConfig(wrapConfig(configFile)) {
            val list: List<String> by config(path, listOf<String>())
        }
        return stored.list
    }

    private fun loggerWith(handler: Handler): Logger =
        Logger.getLogger("kraftlin-test-${System.nanoTime()}").apply {
            useParentHandlers = false
            addHandler(handler)
        }

    private class RecordingHandler : Handler() {
        val messages = mutableListOf<String>()

        override fun publish(record: LogRecord) {
            if (record.level == Level.WARNING) messages += record.message
        }

        override fun flush() {}

        override fun close() {}
    }

    private companion object {
        val MINEABLE_PICKAXE_KEY: NamespacedKey = NamespacedKey.minecraft("mineable/pickaxe")

        /** Stand-in for the server's `#minecraft:mineable/pickaxe` block tag, which [Tag.MINEABLE_PICKAXE] resolves to. */
        val MINEABLE_PICKAXE: Tag<Material> = mockk {
            every { key } returns MINEABLE_PICKAXE_KEY
            every { values } returns setOf(Material.STONE, Material.IRON_ORE)
        }

        val ENCHANTABLE_SWORD_KEY: NamespacedKey = NamespacedKey.minecraft("enchantable/sword")

        /** Stand-in for the server's `#minecraft:enchantable/sword` item tag, which has no block tag of that name. */
        val ENCHANTABLE_SWORD: Tag<Material> = mockk {
            every { key } returns ENCHANTABLE_SWORD_KEY
            every { values } returns setOf(Material.IRON_SWORD, Material.DIAMOND_SWORD)
        }
    }
}
