@file:Suppress("unused")

package io.github.kraftlin.config

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.io.TempDir
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class YamlConfigWrapperTest {

    @TempDir
    lateinit var dir: Path

    // ========================= Type coercion: getInt =========================

    @Test
    fun `getInt with int value`() {
        val wrapper = wrapperWith("value: 42")
        wrapper.addDefault("value", 0)
        assertEquals(42, wrapper.getInt("value"))
    }

    @Test
    fun `getInt with long in range`() {
        val wrapper = wrapperWith("value: 100")
        wrapper.addDefault("value", 0)
        assertEquals(100, wrapper.getInt("value"))
    }

    @Test
    fun `getInt with double no fraction`() {
        val wrapper = wrapperWith("value: 3.0")
        wrapper.addDefault("value", 0)
        assertEquals(3, wrapper.getInt("value"))
    }

    @Test
    fun `getInt with parseable string`() {
        val wrapper = wrapperWith("value: '42'")
        wrapper.addDefault("value", 0)
        assertEquals(42, wrapper.getInt("value"))
    }

    @Test
    fun `getInt with unparseable string warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: hello")
        wrapper.addDefault("value", 99)
        assertEquals(99, wrapper.getInt("value"))
        assertTrue(warnings.isNotEmpty(), "Should log a warning")
    }

    @Test
    fun `getInt with boolean warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: true")
        wrapper.addDefault("value", 99)
        assertEquals(99, wrapper.getInt("value"))
        assertTrue(warnings.isNotEmpty())
    }

    @Test
    fun `getInt with null returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: ~")
        wrapper.addDefault("value", 99)
        assertEquals(99, wrapper.getInt("value"))
        assertTrue(warnings.isEmpty(), "null should return default silently")
    }

    @Test
    fun `getInt with double with fraction warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: 3.5")
        wrapper.addDefault("value", 99)
        assertEquals(99, wrapper.getInt("value"))
        assertTrue(warnings.isNotEmpty())
    }

    // ========================= Type coercion: getLong =========================

    @Test
    fun `getLong with long value`() {
        val wrapper = wrapperWith("value: 123456789")
        wrapper.addDefault("value", 0L)
        assertEquals(123456789L, wrapper.getLong("value"))
    }

    @Test
    fun `getLong with int widening`() {
        val wrapper = wrapperWith("value: 42")
        wrapper.addDefault("value", 0L)
        assertEquals(42L, wrapper.getLong("value"))
    }

    @Test
    fun `getLong with parseable string`() {
        val wrapper = wrapperWith("value: '999'")
        wrapper.addDefault("value", 0L)
        assertEquals(999L, wrapper.getLong("value"))
    }

    @Test
    fun `getLong with unparseable string warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: abc")
        wrapper.addDefault("value", 77L)
        assertEquals(77L, wrapper.getLong("value"))
        assertTrue(warnings.isNotEmpty())
    }

    // ========================= Type coercion: getDouble =========================

    @Test
    fun `getDouble with double value`() {
        val wrapper = wrapperWith("value: 3.14")
        wrapper.addDefault("value", 0.0)
        assertEquals(3.14, wrapper.getDouble("value"))
    }

    @Test
    fun `getDouble with int`() {
        val wrapper = wrapperWith("value: 5")
        wrapper.addDefault("value", 0.0)
        assertEquals(5.0, wrapper.getDouble("value"))
    }

    @Test
    fun `getDouble with parseable string`() {
        val wrapper = wrapperWith("value: '2.718'")
        wrapper.addDefault("value", 0.0)
        assertEquals(2.718, wrapper.getDouble("value"))
    }

    @Test
    fun `getDouble with unparseable string warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: xyz")
        wrapper.addDefault("value", 1.0)
        assertEquals(1.0, wrapper.getDouble("value"))
        assertTrue(warnings.isNotEmpty())
    }

    // ========================= Type coercion: getString =========================

    @Test
    fun `getString with string value`() {
        val wrapper = wrapperWith("value: hello")
        wrapper.addDefault("value", "")
        assertEquals("hello", wrapper.getString("value"))
    }

    @Test
    fun `getString with int auto-converts`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: 42")
        wrapper.addDefault("value", "")
        assertEquals("42", wrapper.getString("value"))
        assertTrue(warnings.isEmpty(), "Safe coercion should not warn")
    }

    @Test
    fun `getString with double auto-converts`() {
        val wrapper = wrapperWith("value: 3.14")
        wrapper.addDefault("value", "")
        assertEquals("3.14", wrapper.getString("value"))
    }

    @Test
    fun `getString with boolean auto-converts`() {
        val wrapper = wrapperWith("value: true")
        wrapper.addDefault("value", "")
        assertEquals("true", wrapper.getString("value"))
    }

    @Test
    fun `getString with null returns default`() {
        val wrapper = wrapperWith("value: ~")
        wrapper.addDefault("value", "fallback")
        assertEquals("fallback", wrapper.getString("value"))
    }

    // ========================= Type coercion: getBoolean =========================

    @Test
    fun `getBoolean with true`() {
        val wrapper = wrapperWith("value: true")
        wrapper.addDefault("value", false)
        assertEquals(true, wrapper.getBoolean("value"))
    }

    @Test
    fun `getBoolean with false`() {
        val wrapper = wrapperWith("value: false")
        wrapper.addDefault("value", true)
        assertEquals(false, wrapper.getBoolean("value"))
    }

    @Test
    fun `getBoolean with string true`() {
        val wrapper = wrapperWith("value: 'true'")
        wrapper.addDefault("value", false)
        assertEquals(true, wrapper.getBoolean("value"))
    }

    @Test
    fun `getBoolean with string false`() {
        val wrapper = wrapperWith("value: 'false'")
        wrapper.addDefault("value", true)
        assertEquals(false, wrapper.getBoolean("value"))
    }

    @Test
    fun `getBoolean with int warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: 1")
        wrapper.addDefault("value", false)
        assertEquals(false, wrapper.getBoolean("value"))
        assertTrue(warnings.isNotEmpty(), "Number to boolean is a mismatch")
    }

    // ========================= YAML 1.2 booleans =========================

    @Test
    fun `yes is string not boolean`() {
        val wrapper = wrapperWith("value: yes")
        assertEquals("yes", wrapper.getString("value"))
    }

    @Test
    fun `no is string not boolean`() {
        val wrapper = wrapperWith("value: no")
        assertEquals("no", wrapper.getString("value"))
    }

    @Test
    fun `on is string not boolean`() {
        val wrapper = wrapperWith("value: on")
        assertEquals("on", wrapper.getString("value"))
    }

    @Test
    fun `off is string not boolean`() {
        val wrapper = wrapperWith("value: off")
        assertEquals("off", wrapper.getString("value"))
    }

    @Test
    fun `True is boolean`() {
        val wrapper = wrapperWith("value: True")
        wrapper.addDefault("value", false)
        assertEquals(true, wrapper.getBoolean("value"))
    }

    @Test
    fun `FALSE is boolean`() {
        val wrapper = wrapperWith("value: FALSE")
        wrapper.addDefault("value", true)
        assertEquals(false, wrapper.getBoolean("value"))
    }

    // ========================= List types =========================

    @Test
    fun `getIntegerList happy path`() {
        val wrapper = wrapperWith("values:\n- 1\n- 2\n- 3")
        assertEquals(listOf(1, 2, 3), wrapper.getIntegerList("values"))
    }

    @Test
    fun `getStringList happy path`() {
        val wrapper = wrapperWith("values:\n- a\n- b\n- c")
        assertEquals(listOf("a", "b", "c"), wrapper.getStringList("values"))
    }

    @Test
    fun `getLongList happy path`() {
        val wrapper = wrapperWith("values:\n- 100\n- 200")
        assertEquals(listOf(100L, 200L), wrapper.getLongList("values"))
    }

    @Test
    fun `getDoubleList happy path`() {
        val wrapper = wrapperWith("values:\n- 1.1\n- 2.2")
        assertEquals(listOf(1.1, 2.2), wrapper.getDoubleList("values"))
    }

    @Test
    fun `getBooleanList happy path`() {
        val wrapper = wrapperWith("values:\n- true\n- false")
        assertEquals(listOf(true, false), wrapper.getBooleanList("values"))
    }

    @Test
    fun `scalar where list expected returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: hello")
        wrapper.addDefault("value", listOf("a", "b"))
        assertEquals(listOf("a", "b"), wrapper.getStringList("value"))
        assertTrue(warnings.isNotEmpty())
    }

    // ========================= getKeys =========================

    @Test
    fun `getKeys deep false returns top-level only`() {
        val wrapper = wrapperWith("top1: v\ntop2: v\nsection:\n  nested: v")
        assertEquals(setOf("top1", "top2", "section"), wrapper.getKeys(deep = false))
    }

    @Test
    fun `getKeys deep true returns all leaf paths`() {
        val wrapper = wrapperWith("top: v\na:\n  b:\n    c:\n      d: v")
        assertEquals(setOf("top", "a.b.c.d"), wrapper.getKeys(deep = true))
    }

    @Test
    fun `getKeys deep true on empty config`() {
        val file = dir.resolve("empty.yml")
        val wrapper = wrapConfig(file)
        assertEquals(emptySet(), wrapper.getKeys(deep = true))
    }

    // ========================= remove =========================

    @Test
    fun `remove top-level key`() {
        val file = dir.resolve("remove-top.yml")
        Files.writeString(file, "keep: v\nremove: gone\n")
        val wrapper = wrapConfig(file)
        wrapper.remove("remove")
        wrapper.save()
        val content = Files.readString(file)
        assertTrue(content.contains("keep: v"))
        assertFalse(content.contains("remove"))
    }

    @Test
    fun `remove nested key preserves sibling`() {
        val file = dir.resolve("remove-sibling.yml")
        Files.writeString(file, "section:\n  keep: v\n  remove: gone\n")
        val wrapper = wrapConfig(file)
        wrapper.remove("section.remove")
        wrapper.save()
        val content = Files.readString(file)
        assertTrue(content.contains("keep: v"))
        assertFalse(content.contains("remove"))
    }

    @Test
    fun `remove nested key cleans up empty parents`() {
        val file = dir.resolve("remove-deep.yml")
        Files.writeString(file, "keep: v\na:\n  b:\n    c: gone\n")
        val wrapper = wrapConfig(file)
        wrapper.remove("a.b.c")
        wrapper.save()
        val content = Files.readString(file)
        assertTrue(content.contains("keep: v"))
        assertFalse(content.contains("a:"))
    }

    @Test
    fun `remove non-existent path does not throw`() {
        val wrapper = wrapperWith("key: v")
        wrapper.remove("does.not.exist")
    }

    // ========================= getMap =========================

    @Test
    fun `getMap returns flat values`() {
        val wrapper = wrapperWith("map:\n  k1: v1\n  k2: v2")
        assertEquals(mapOf("k1" to "v1", "k2" to "v2"), wrapper.getMap("map"))
    }

    @Test
    fun `getMap filters nested sections`() {
        val wrapper = wrapperWith("map:\n  leaf: v\n  nested:\n    deep: v")
        val map = wrapper.getMap("map")
        assertEquals(mapOf("leaf" to "v"), map)
        assertFalse(map.containsKey("nested"), "Nested sections should be filtered out")
    }

    // ========================= saveDefaults =========================

    @Test
    fun `saveDefaults with deeply nested defaults`() {
        val file = dir.resolve("config.yml")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val shallow by config("shallow", "v1")
            val deep by config("a.b.c", "v2")
        }
        config.saveDefaults()
        val content = Files.readString(file)
        assertTrue(content.contains("shallow: v1"))
        assertTrue(content.contains("c: v2"))
    }

    @Test
    fun `saveDefaults does not override existing values`() {
        val file = dir.resolve("config.yml")
        Files.writeString(file, "setting: custom\n")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val setting by config("setting", "default")
        }
        config.saveDefaults()
        config.reloadConfig()
        assertEquals("custom", config.setting)
    }

    @Test
    fun `saveDefaults merges with partially overlapping config`() {
        val file = dir.resolve("config.yml")
        Files.writeString(file, "section:\n  existing: keep\n")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val existing by config("section.existing", "default")
            val added by config("section.added", "new")
        }
        config.saveDefaults()
        val content = Files.readString(file)
        assertTrue(content.contains("existing: keep"))
        assertTrue(content.contains("added: new"))
    }

    @Test
    fun `saveDefaults creates parent directories`() {
        val file = dir.resolve("sub/dir/config.yml")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val v by config("key", "value")
        }
        config.saveDefaults()
        assertTrue(Files.exists(file))
    }

    // ========================= Comments =========================

    @Test
    fun `comments round-trip through save and reload`() {
        val file = dir.resolve("config.yml")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val setting by config("setting", "value", "My comment")
        }
        config.saveDefaults()
        val content = Files.readString(file)
        assertTrue(content.contains("# My comment"), "Comment should be in file: $content")

        val wrapper2 = wrapConfig(file)
        assertEquals(listOf("My comment"), wrapper2.getComments("setting"))
    }

    @Test
    fun `existing comments preserved when not overwritten`() {
        val file = dir.resolve("config.yml")
        Files.writeString(file, "# Old comment\nsetting: value\n")

        val wrapper = wrapConfig(file)
        assertEquals(listOf("Old comment"), wrapper.getComments("setting"))
    }

    @Test
    fun `comments on nested paths`() {
        val file = dir.resolve("config.yml")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val nested by config("section.key", "value", "Nested comment")
        }
        config.saveDefaults()
        val content = Files.readString(file)
        assertTrue(content.contains("# Nested comment"))
    }

    // ========================= Integration =========================

    @Test
    fun `reload after external edit does not overwrite file`() {
        val file = dir.resolve("config.yml")
        val config = object : AbstractConfig(wrapConfig(file)) {
            var name: String by config("name", "default")
            var count: Int by config("count", 1)
        }
        config.saveDefaults()
        assertEquals("default", config.name)

        // Simulate an operator editing the file by hand
        @Language("yaml")
        val edited = """
            |name: custom
            |count: 99
            |""".trimMargin()
        Files.writeString(file, edited)

        config.reloadConfig()

        // New values must be loaded
        assertEquals("custom", config.name)
        assertEquals(99, config.count)

        // File on disk must still contain the hand-edited content, not be overwritten
        val content = Files.readString(file)
        assertEquals(edited, content)
    }

    @Test
    fun `full save reload edit cycle`() {
        val file = dir.resolve("config.yml")
        val config = object : AbstractConfig(wrapConfig(file)) {
            var name: String by config("name", "default")
            val count: Int by config("count", 42)
            var items: List<String> by config("items", listOf("a", "b"))
        }
        config.saveDefaults()

        assertEquals("default", config.name)
        assertEquals(42, config.count)
        assertEquals(listOf("a", "b"), config.items)

        // Edit file externally
        @Language("yaml")
        val edited = """
            |name: edited
            |count: 100
            |items:
            |- x
            |- y
            |- z
            |""".trimMargin()
        Files.writeString(file, edited)
        config.reloadConfig()

        assertEquals("edited", config.name)
        assertEquals(100, config.count)
        assertEquals(listOf("x", "y", "z"), config.items)

        // Modify via delegate and save
        config.name = "programmatic"
        config.items = listOf("only")
        config.save()

        val content = Files.readString(file)
        assertTrue(content.contains("name: programmatic"))
        assertTrue(content.contains("- only"))
    }

    @Test
    fun `load from non-existing path uses defaults`() {
        val file = dir.resolve("does-not-exist.yml")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val v by config("key", "default")
        }
        assertEquals("default", config.v)
    }

    // ========================= Warning verification =========================

    @Test
    fun `safe coercion does not warn`() {
        val (wrapper, warnings) = wrapperWithWarnings("num: 42\nbool: true")
        wrapper.addDefault("num", "")
        wrapper.addDefault("bool", "")
        wrapper.getString("num")   // int → string: safe
        wrapper.getString("bool")  // bool → string: safe
        assertTrue(warnings.isEmpty(), "Safe coercion should not produce warnings")
    }

    @Test
    fun `type mismatch warns`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: not-a-number")
        wrapper.addDefault("value", 0)
        wrapper.getInt("value")
        assertTrue(warnings.isNotEmpty(), "Type mismatch should produce a warning")
        assertTrue(warnings.first().contains("not-a-number"), "Warning should mention the actual value")
    }

    // ========================= Error handling =========================

    @Test
    fun `malformed YAML on initial load throws ConfigException`() {
        val file = dir.resolve("malformed.yml")
        Files.writeString(file, "key: [unclosed")

        val exception = assertFailsWith<ConfigException> { wrapConfig(file) }
        assertTrue(exception.message!!.contains(file.toString()), "Message should contain file path")
        assertTrue(exception.message!!.contains("invalid YAML"), "Message should mention invalid YAML")
        assertIs<YamlEngineException>(exception.cause, "Cause should be YamlEngineException")
        assertEquals(file, exception.path, "Exception path should match config file")
    }

    @Test
    fun `malformed YAML on reload preserves old data and throws ConfigException`() {
        val file = dir.resolve("reload-fail.yml")
        Files.writeString(file, "key: original")

        val wrapper = wrapConfig(file)
        wrapper.addDefault("key", "")
        assertEquals("original", wrapper.getString("key"))

        // Overwrite with malformed YAML
        Files.writeString(file, "key: [unclosed")

        assertFailsWith<ConfigException> { wrapper.reloadConfig() }

        // Old data should still be intact
        assertEquals("original", wrapper.getString("key"))
    }

    @Test
    fun `reload with deleted file resets to empty data`() {
        val file = dir.resolve("delete-me.yml")
        Files.writeString(file, "key: value")

        val wrapper = wrapConfig(file)
        assertEquals(setOf("key"), wrapper.getKeys(deep = true))

        Files.delete(file)
        wrapper.reloadConfig()

        assertEquals(emptySet(), wrapper.getKeys(deep = true))
    }

    @Test
    fun `save failure throws ConfigException with data intact`() {
        // Start with a valid file so the wrapper initializes with data
        val file = dir.resolve("save-fail.yml")
        Files.writeString(file, "key: value")
        val wrapper = wrapConfig(file)
        wrapper.addDefault("key", "")
        assertEquals("value", wrapper.getString("key"))

        // Make the file unsaveable by replacing it with a directory
        Files.delete(file)
        Files.createDirectory(file)

        val exception = assertFailsWith<ConfigException> { wrapper.save() }
        assertTrue(exception.message!!.contains(file.toString()))
        assertTrue(exception.message!!.contains("Could not save"))

        // Data is still intact
        assertEquals("value", wrapper.getString("key"))
    }

    @Test
    fun `ConfigException contains file path property`() {
        val file = dir.resolve("bad.yml")
        Files.writeString(file, "key: [unclosed")

        val exception = assertFailsWith<ConfigException> { wrapConfig(file) }
        assertEquals(file, exception.path)
    }

    // ========================= Helpers =========================

    private fun wrapperWith(yaml: String): AbstractConfig.ConfigWrapper {
        val file = dir.resolve("config-${System.nanoTime()}.yml")
        Files.writeString(file, yaml)
        return wrapConfig(file)
    }

    private fun wrapperWithWarnings(yaml: String): Pair<AbstractConfig.ConfigWrapper, MutableList<String>> {
        val file = dir.resolve("config-${System.nanoTime()}.yml")
        Files.writeString(file, yaml)
        val warnings = mutableListOf<String>()
        val logger = Logger.getLogger("test-${System.nanoTime()}")
        logger.useParentHandlers = false
        logger.addHandler(object : Handler() {
            override fun publish(record: LogRecord) {
                if (record.level == Level.WARNING) warnings.add(record.message)
            }
            override fun flush() {}
            override fun close() {}
        })
        return wrapConfig(file, logger) to warnings
    }

}
