package io.github.kraftlin.config.bungee

import io.github.kraftlin.config.AbstractConfig
import io.github.kraftlin.config.AbstractConfig.ConfigWrapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BungeeConfigWrapperTest {

    @TempDir
    lateinit var directory: Path

    // --- getKeys ---

    @Test
    fun `getKeys deep false returns only top-level keys`() {
        val configFile = directory.resolve("config.yml")
        @Language("yaml")
        val content = """
            |top1: value
            |top2: value
            |section:
            |  nested: value
            |  deep:
            |    leaf: value
        """.trimMargin()
        Files.writeString(configFile, content)

        val wrapper = wrapConfig(configFile)
        val keys = wrapper.getKeys(deep = false)

        assertEquals(setOf("top1", "top2", "section"), keys)
    }

    @Test
    fun `getKeys deep true returns all leaf paths`() {
        val configFile = directory.resolve("config.yml")
        @Language("yaml")
        val content = """
            |top: value
            |level1:
            |  level2a: value
            |  level2b:
            |    level3: value
            |    level3b:
            |      level4: value
        """.trimMargin()
        Files.writeString(configFile, content)

        val wrapper = wrapConfig(configFile)
        val keys = wrapper.getKeys(deep = true)

        assertEquals(
            setOf("top", "level1.level2a", "level1.level2b.level3", "level1.level2b.level3b.level4"),
            keys
        )
    }

    @Test
    fun `getKeys deep true on empty config returns empty set`() {
        val configFile = directory.resolve("empty.yml")
        // File does not exist — wrapper returns empty default config
        val wrapper = wrapConfig(configFile)

        assertEquals(emptySet(), wrapper.getKeys(deep = true))
    }

    // --- remove ---

    @Test
    fun `remove top-level key`() {
        val configFile = directory.resolve("config.yml")
        Files.writeString(configFile, "keep: value\nremove_me: gone\n")

        val wrapper = wrapConfig(configFile)
        wrapper.remove("remove_me")
        wrapper.save()

        val content = Files.readString(configFile)
        assertTrue(content.contains("keep: value"))
        assertFalse(content.contains("remove_me"))
    }

    @Test
    fun `remove nested key with remaining sibling`() {
        val configFile = directory.resolve("config.yml")
        @Language("yaml")
        val content = """
            |section:
            |  keep: value
            |  remove_me: gone
        """.trimMargin()
        Files.writeString(configFile, content)

        val wrapper = wrapConfig(configFile)
        wrapper.remove("section.remove_me")
        wrapper.save()

        val result = Files.readString(configFile)
        assertTrue(result.contains("section:"), "Parent section with remaining sibling should be preserved")
        assertTrue(result.contains("keep: value"))
        assertFalse(result.contains("remove_me"))
    }

    @Test
    fun `remove nested key cleaning up empty parents`() {
        val configFile = directory.resolve("config.yml")
        @Language("yaml")
        val content = """
            |keep: value
            |a:
            |  b:
            |    c: remove_me
        """.trimMargin()
        Files.writeString(configFile, content)

        val wrapper = wrapConfig(configFile)
        wrapper.remove("a.b.c")
        wrapper.save()

        val result = Files.readString(configFile)
        assertTrue(result.contains("keep: value"))
        assertFalse(result.contains("a:"), "Empty ancestor sections should be cleaned up")
        assertFalse(result.contains("b:"))
        assertFalse(result.contains("c:"))
    }

    @Test
    fun `remove non-existent path does not throw`() {
        val configFile = directory.resolve("config.yml")
        Files.writeString(configFile, "key: value\n")

        val wrapper = wrapConfig(configFile)
        wrapper.remove("does.not.exist")
        wrapper.save()

        val result = Files.readString(configFile)
        assertTrue(result.contains("key: value"))
    }

    // --- getMap ---

    @Test
    fun `getMap filters out nested Configuration sections`() {
        val configFile = directory.resolve("config.yml")
        @Language("yaml")
        val content = """
            |mymap:
            |  leaf1: value1
            |  leaf2: value2
            |  nested_section:
            |    deep: should_be_filtered
        """.trimMargin()
        Files.writeString(configFile, content)

        val wrapper = wrapConfig(configFile)
        val map = wrapper.getMap("mymap")

        assertEquals(mapOf("leaf1" to "value1", "leaf2" to "value2"), map)
        assertFalse(map.containsKey("nested_section"), "Nested Configuration sections should be filtered out")
    }

    // --- saveDefaults ---

    @Test
    fun `saveDefaults with deeply nested defaults`() {
        val configFile = directory.resolve("config.yml")

        val config = object : AbstractConfig(wrapConfig(configFile)) {
            val shallow by config("shallow", "v1")
            val deep by config("a.b.c", "v2")
            val deeper by config("x.y.z.w", "v3")
        }

        config.saveDefaults()

        val content = Files.readString(configFile)
        assertTrue(content.contains("shallow: v1"))
        assertTrue(content.contains("c: v2"))
        assertTrue(content.contains("w: v3"))
    }

    @Test
    fun `saveDefaults merges with partially overlapping existing config`() {
        val configFile = directory.resolve("config.yml")
        @Language("yaml")
        val existing = """
            |section:
            |  existing: keep_me
        """.trimMargin()
        Files.writeString(configFile, existing)

        val config = object : AbstractConfig(wrapConfig(configFile)) {
            val existing by config("section.existing", "default")
            val added by config("section.added", "new_value")
            val top by config("top", "top_value")
        }

        config.saveDefaults()

        val content = Files.readString(configFile)
        assertTrue(content.contains("existing: keep_me"), "Pre-existing value should be preserved")
        assertTrue(content.contains("added: new_value"), "New default should be added")
        assertTrue(content.contains("top: top_value"), "Top-level default should be added")
    }

    // --- Type round-trips ---

    @Test
    fun `round-trip all primitive types`() {
        val configFile = directory.resolve("config.yml")

        val config = object : AbstractConfig(wrapConfig(configFile)) {
            val b by config("bool", true)
            val i by config("int", 42)
            val l by config("long", 123456789L)
            val d by config("double", 3.14)
            val s by config("string", "hello world")
        }

        config.saveDefaults()
        config.reloadConfig()

        assertEquals(true, config.b)
        assertEquals(42, config.i)
        assertEquals(123456789L, config.l)
        assertEquals(3.14, config.d)
        assertEquals("hello world", config.s)
    }

    @Test
    fun `round-trip list types`() {
        val configFile = directory.resolve("config.yml")

        @Suppress("RemoveExplicitTypeArguments")
        val config = object : AbstractConfig(wrapConfig(configFile)) {
            val ints: List<Int> by config("ints", listOf<Int>(1, 2, 3))
            val longs: List<Long> by config("longs", listOf<Long>(100L, 200L))
            val doubles: List<Double> by config("doubles", listOf(1.1, 2.2))
            val strings: List<String> by config("strings", listOf("a", "b", "c"))
        }

        config.saveDefaults()
        config.reloadConfig()

        assertEquals(listOf(1, 2, 3), config.ints)
        assertEquals(listOf(100L, 200L), config.longs)
        assertEquals(listOf(1.1, 2.2), config.doubles)
        assertEquals(listOf("a", "b", "c"), config.strings)
    }

    @Test
    fun `round-trip empty list`() {
        val configFile = directory.resolve("config.yml")
        // Write a config with an empty list, then reload
        Files.writeString(configFile, "items: []\n")

        val config = object : AbstractConfig(wrapConfig(configFile)) {
            val items: List<String> by config("items", listOf("default"))
        }

        assertEquals(emptyList(), config.items)
    }

    // --- Comment no-ops ---

    @Test
    fun `getComments returns empty list`() {
        val wrapper = wrapConfig(directory.resolve("config.yml"))
        assertEquals(emptyList(), wrapper.getComments("any.path"))
    }

    @Test
    fun `setComments does not throw`() {
        val wrapper = wrapConfig(directory.resolve("config.yml"))
        wrapper.setComments("any.path", listOf("comment1", "comment2"))
        // No exception = pass
    }
}
