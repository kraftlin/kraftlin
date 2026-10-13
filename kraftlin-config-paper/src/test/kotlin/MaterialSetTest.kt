package io.github.kraftlin.config.paper

import io.mockk.every
import io.mockk.mockk
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class MaterialSetTest {

    private val logs = tag("logs", Material.OAK_LOG, Material.BIRCH_LOG)
    private val planks = tag("planks", Material.OAK_PLANKS, Material.BIRCH_PLANKS)
    private val burnable = tag("burnable", Material.OAK_LOG, Material.OAK_PLANKS)

    @Test
    fun `contains the materials and the values of all tags`() {
        val set = MaterialSet(listOf(Material.STONE), listOf(logs))

        assertEquals(setOf(Material.STONE, Material.OAK_LOG, Material.BIRCH_LOG), set)
        assertEquals(setOf(Material.STONE), set.materials)
        assertEquals(setOf(logs), set.tags)
        assertTrue(Material.OAK_LOG in set)
    }

    @Test
    fun `is empty by default`() {
        val set = MaterialSet()

        assertTrue(set.isEmpty())
        assertEquals(emptySet(), set.materials)
        assertEquals(emptySet(), set.tags)
    }

    @Test
    fun `tags are deduplicated by key keeping insertion order`() {
        val otherLogs = tag("logs", Material.SPRUCE_LOG)

        val set = MaterialSet(tags = listOf(planks, logs, otherLogs))

        assertEquals(listOf(planks, logs), set.tags.toList())
        assertEquals(setOf(Material.OAK_PLANKS, Material.BIRCH_PLANKS, Material.OAK_LOG, Material.BIRCH_LOG), set)
    }

    @Test
    fun `tags are resolved when the set is created`() {
        var members = setOf(Material.OAK_LOG)
        val growing: Tag<Material> = mockk {
            every { key } returns NamespacedKey.minecraft("growing")
            every { values } answers { members }
        }
        val set = MaterialSet(tags = listOf(growing))

        members = setOf(Material.OAK_LOG, Material.BIRCH_LOG)

        assertEquals(setOf(Material.OAK_LOG), set)
        assertEquals(setOf(Material.OAK_LOG, Material.BIRCH_LOG), MaterialSet(tags = listOf(growing)))
    }

    @Test
    fun `is a copy of the given collections`() {
        val materials = mutableListOf(Material.STONE)
        val tags = mutableListOf(logs)
        val set = MaterialSet(materials, tags)

        materials += Material.DIRT
        tags += planks

        assertEquals(setOf(Material.STONE), set.materials)
        assertEquals(setOf(logs), set.tags)
        assertEquals(setOf(Material.STONE, Material.OAK_LOG, Material.BIRCH_LOG), set)
    }

    @Test
    fun `cannot be modified`() {
        val set = MaterialSet(listOf(Material.STONE), listOf(logs))

        assertFailsWith<UnsupportedOperationException> { (set.materials as MutableSet<Material>).add(Material.DIRT) }
        assertFailsWith<UnsupportedOperationException> { (set.tags as MutableSet<Tag<Material>>).add(planks) }
        assertFailsWith<UnsupportedOperationException> {
            val iterator = set.iterator() as MutableIterator<Material>
            iterator.next()
            iterator.remove()
        }
    }

    @Test
    fun `equality follows the set contract and ignores tags`() {
        val withTag = MaterialSet(tags = listOf(logs))
        val listed = MaterialSet(listOf(Material.OAK_LOG, Material.BIRCH_LOG))
        val plain = setOf(Material.OAK_LOG, Material.BIRCH_LOG)

        assertEquals<Set<Material>>(withTag, listed)
        assertEquals<Set<Material>>(withTag, plain)
        assertEquals<Set<Material>>(plain, withTag)
        assertEquals(plain.hashCode(), withTag.hashCode())
        assertEquals(listed.hashCode(), withTag.hashCode())
        assertNotEquals<Set<Material>>(withTag, MaterialSet(tags = listOf(planks)))
    }

    @Test
    fun `toString shows materials and tags`() {
        val set = MaterialSet(listOf(Material.STONE), listOf(logs))

        assertEquals("MaterialSet(materials=[minecraft:stone], tags=[#minecraft:logs])", set.toString())
    }

    @Test
    fun `legacy materials are rejected`() {
        @Suppress("DEPRECATION")
        val exception = assertFailsWith<IllegalArgumentException> {
            materialSetOf(Material.STONE, Material.LEGACY_STONE)
        }

        assertEquals("Legacy materials are not supported: LEGACY_STONE", exception.message)
    }

    @Test
    fun `tagsContaining returns the tags of this set that contain the material`() {
        val set = MaterialSet(listOf(Material.STONE), listOf(logs, planks, burnable))

        assertEquals(setOf(logs, burnable), set.tagsContaining(Material.OAK_LOG))
        assertEquals(setOf(planks), set.tagsContaining(Material.BIRCH_PLANKS))
        assertEquals(emptySet(), set.tagsContaining(Material.STONE))
        assertEquals(emptySet(), set.tagsContaining(Material.DIRT))
    }

    @Test
    fun `materialSetOf creates a set of the materials without tags`() {
        val set = materialSetOf(Material.STONE, Material.DIRT, Material.STONE)

        assertEquals(setOf(Material.STONE, Material.DIRT), set.materials)
        assertEquals(emptySet(), set.tags)
        assertEquals(MaterialSet(), materialSetOf())
    }

    @Test
    fun `plus adds materials`() {
        val set = MaterialSet(listOf(Material.STONE), listOf(logs))

        val single: MaterialSet = set + Material.DIRT
        val iterable: MaterialSet = set + listOf(Material.DIRT, Material.SAND)
        val array: MaterialSet = set + arrayOf(Material.DIRT, Material.SAND)
        val sequence: MaterialSet = set + sequenceOf(Material.DIRT, Material.SAND)

        assertEquals(setOf(Material.STONE, Material.DIRT), single.materials)
        assertEquals(setOf(Material.STONE, Material.DIRT, Material.SAND), iterable.materials)
        assertEquals(setOf(Material.STONE, Material.DIRT, Material.SAND), array.materials)
        assertEquals(setOf(Material.STONE, Material.DIRT, Material.SAND), sequence.materials)
        for (result in listOf(single, iterable, array, sequence)) {
            assertEquals(setOf(logs), result.tags)
            assertTrue(Material.OAK_LOG in result)
        }
        assertEquals(setOf(Material.STONE), set.materials, "The original set is unchanged")
    }

    @Test
    fun `plus adds tags`() {
        val set = MaterialSet(listOf(Material.STONE), listOf(logs))

        val result: MaterialSet = set + planks

        assertEquals(listOf(logs, planks), result.tags.toList())
        assertEquals(setOf(Material.STONE), result.materials)
        assertTrue(Material.OAK_PLANKS in result)
        assertEquals(setOf(logs), set.tags, "The original set is unchanged")
    }

    @Test
    fun `plus a tag that is already part of the set keeps a single tag`() {
        val set = MaterialSet(tags = listOf(logs))

        val result = set + tag("logs", Material.SPRUCE_LOG)

        assertEquals(listOf(logs), result.tags.toList())
    }

    @Test
    fun `plus a material set merges materials and tags`() {
        val set = MaterialSet(listOf(Material.STONE), listOf(logs))
        val other = MaterialSet(listOf(Material.DIRT), listOf(planks, logs))
        val otherAsSet: Set<Material> = other

        val result: MaterialSet = set + other
        val resultFromSet: MaterialSet = set + otherAsSet

        for (merged in listOf(result, resultFromSet)) {
            assertEquals(setOf(Material.STONE, Material.DIRT), merged.materials)
            assertEquals(listOf(logs, planks), merged.tags.toList())
        }
    }

    @Test
    fun `minus a material that is not part of the set changes nothing`() {
        val set = MaterialSet(listOf(Material.STONE), listOf(logs))

        val result: MaterialSet = set - Material.DIRT

        assertEquals(setOf(Material.STONE), result.materials)
        assertEquals(setOf(logs), result.tags)
    }

    @Test
    fun `minus a listed material removes it`() {
        val set = MaterialSet(listOf(Material.STONE, Material.DIRT), listOf(logs))

        val result: MaterialSet = set - Material.STONE

        assertEquals(setOf(Material.DIRT), result.materials)
        assertEquals(setOf(logs), result.tags)
        assertEquals(setOf(Material.STONE, Material.DIRT), set.materials, "The original set is unchanged")
    }

    @Test
    fun `minus a material included by a tag throws`() {
        val set = MaterialSet(listOf(Material.STONE), listOf(logs))

        val exception = assertFailsWith<IllegalArgumentException> { set - Material.OAK_LOG }

        assertEquals("OAK_LOG is included by tag #minecraft:logs; remove the tag instead", exception.message)
    }

    @Test
    fun `minus a material included by a tag throws even if it is listed too`() {
        val set = MaterialSet(listOf(Material.OAK_LOG), listOf(logs, planks, burnable))

        val exception = assertFailsWith<IllegalArgumentException> { set - Material.OAK_LOG }

        assertEquals(
            "OAK_LOG is included by tag #minecraft:logs, #minecraft:burnable; remove the tag instead",
            exception.message
        )
    }

    @Test
    fun `minus a material that a tag includes since the set was created throws`() {
        var members = setOf(Material.OAK_LOG)
        val growing: Tag<Material> = mockk {
            every { key } returns NamespacedKey.minecraft("growing")
            every { values } answers { members }
        }
        val set = MaterialSet(tags = listOf(growing))

        members = setOf(Material.OAK_LOG, Material.CHERRY_LOG)

        // The result resolves the tag again, so it could not leave out CHERRY_LOG
        val exception = assertFailsWith<IllegalArgumentException> { set - Material.CHERRY_LOG }
        assertEquals("CHERRY_LOG is included by tag #minecraft:growing; remove the tag instead", exception.message)
    }

    @Test
    fun `minus several materials removes the listed ones and ignores missing ones`() {
        val set = MaterialSet(listOf(Material.STONE, Material.DIRT, Material.SAND), listOf(logs))

        val iterable: MaterialSet = set - listOf(Material.STONE, Material.GRAVEL)
        val array: MaterialSet = set - arrayOf(Material.STONE, Material.GRAVEL)
        val sequence: MaterialSet = set - sequenceOf(Material.STONE, Material.GRAVEL)

        for (result in listOf(iterable, array, sequence)) {
            assertEquals(setOf(Material.DIRT, Material.SAND), result.materials)
            assertEquals(setOf(logs), result.tags)
        }
    }

    @Test
    fun `minus several materials throws if any of them is included by a tag`() {
        val set = MaterialSet(listOf(Material.STONE, Material.DIRT), listOf(logs))

        val removed = listOf(Material.STONE, Material.OAK_LOG)
        for (remove in listOf<() -> MaterialSet>(
            { set - removed },
            { set - removed.toTypedArray() },
            { set - removed.asSequence() }
        )) {
            val exception = assertFailsWith<IllegalArgumentException> { remove() }
            assertEquals("OAK_LOG is included by tag #minecraft:logs; remove the tag instead", exception.message)
        }
        assertEquals(setOf(Material.STONE, Material.DIRT), set.materials, "The original set is unchanged")
    }

    @Test
    fun `minus a tag removes it by key`() {
        val set = MaterialSet(listOf(Material.OAK_LOG), listOf(logs, planks))

        val result: MaterialSet = set - tag("logs")

        assertEquals(setOf(planks), result.tags)
        assertEquals(setOf(Material.OAK_LOG), result.materials)
        assertEquals(setOf(Material.OAK_LOG, Material.OAK_PLANKS, Material.BIRCH_PLANKS), result)
    }

    @Test
    fun `minus a tag that is not part of the set changes nothing`() {
        val set = MaterialSet(listOf(Material.STONE), listOf(logs))

        val result: MaterialSet = set - planks

        assertEquals(setOf(Material.STONE), result.materials)
        assertEquals(setOf(logs), result.tags)
    }

    @Test
    fun `minus a material set removes its tags and materials`() {
        val set = MaterialSet(listOf(Material.STONE, Material.DIRT), listOf(logs, planks))
        val other = MaterialSet(listOf(Material.STONE, Material.OAK_LOG), listOf(logs))
        val otherAsSet: Set<Material> = other

        val result: MaterialSet = set - other
        val resultFromSet: MaterialSet = set - otherAsSet

        for (remaining in listOf(result, resultFromSet)) {
            assertEquals(setOf(Material.DIRT), remaining.materials)
            assertEquals(setOf(planks), remaining.tags)
        }
    }

    @Test
    fun `minus a material set throws if one of its materials is included by a remaining tag`() {
        val set = MaterialSet(listOf(Material.STONE), listOf(logs, planks))
        val other = MaterialSet(listOf(Material.STONE, Material.OAK_PLANKS), listOf(logs))

        val exception = assertFailsWith<IllegalArgumentException> { set - other }

        assertEquals("OAK_PLANKS is included by tag #minecraft:planks; remove the tag instead", exception.message)
    }

    private fun tag(name: String, vararg members: Material): Tag<Material> = mockk {
        every { key } returns NamespacedKey.minecraft(name)
        every { values } returns members.toSet()
    }
}
