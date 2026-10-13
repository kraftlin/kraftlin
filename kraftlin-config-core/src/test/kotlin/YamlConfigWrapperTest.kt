@file:Suppress("unused")

package io.github.kraftlin.config

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assumptions.assumeFalse
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
        assertEquals(
            listOf("Config value at 'value' is not a valid Boolean (found '1'), using the default (false)"),
            warnings,
            "Number to boolean is a mismatch"
        )
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
        assertEquals(listOf("Config value at 'value' is not a list (found 'hello'), using the default ([a, b])"), warnings)
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

    @Test
    fun `getMap returns values as String whatever their source`() {
        val file = dir.resolve("map-types.yml")
        val wrapper = wrapConfig(file)
        wrapper.addDefault("defaults", mapOf("count" to 5, "enabled" to true))
        wrapper.set("values", mapOf("ratio" to 1.5, "id" to 3000000000L))
        val defaults = mapOf("count" to "5", "enabled" to "true")
        val values = mapOf("ratio" to "1.5", "id" to "3000000000")

        assertEquals(defaults, wrapper.getMap("defaults"))
        assertEquals(values, wrapper.getMap("values"))

        wrapper.saveDefaults()
        wrapper.reloadConfig()

        assertEquals(defaults, wrapper.getMap("defaults"))
        assertEquals(values, wrapper.getMap("values"))
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
        assertEquals(
            listOf("Config value at 'value' is not a valid Int (found 'not-a-number'), using the default (0)"),
            warnings,
            "Warning should mention the actual value"
        )
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

    // ========================= Strings as typed =========================

    @Test
    fun `getString returns a float as typed`() {
        val wrapper = wrapperWith("value: 1.10")
        wrapper.addDefault("value", "")
        assertEquals("1.10", wrapper.getString("value"))
    }

    @Test
    fun `getString keeps leading zeros`() {
        val wrapper = wrapperWith("value: 007")
        wrapper.addDefault("value", "")
        assertEquals("007", wrapper.getString("value"))
    }

    @Test
    fun `getString keeps the spelling of booleans`() {
        val wrapper = wrapperWith("value: True")
        wrapper.addDefault("value", "")
        assertEquals("True", wrapper.getString("value"))
    }

    @Test
    fun `getStringList returns elements as typed`() {
        val wrapper = wrapperWith("values: [1.10, 007, True, text]")
        assertEquals(listOf("1.10", "007", "True", "text"), wrapper.getStringList("values"))
    }

    @Test
    fun `getMap returns values as typed`() {
        val wrapper = wrapperWith("map:\n  version: 1.10\n  code: 007")
        assertEquals(mapOf("version" to "1.10", "code" to "007"), wrapper.getMap("map"))
    }

    @Test
    fun `getMap keeps file values as typed after a sibling is set or removed`() {
        val yaml = "m:\n  a: 1.10\n  b: 007\n  c: True\n  x: old\n"
        val set = wrapperWith(yaml)
        set.set("m.z", "new")
        assertEquals(mapOf("a" to "1.10", "b" to "007", "c" to "True", "x" to "old", "z" to "new"), set.getMap("m"))

        val removed = wrapperWith(yaml)
        removed.remove("m.x")
        assertEquals(mapOf("a" to "1.10", "b" to "007", "c" to "True"), removed.getMap("m"))
    }

    @Test
    fun `getMap returns an entry set programmatically as before and its siblings as typed`() {
        val wrapper = wrapperWith("m:\n  a: 1.10\n  b: 007\n")
        wrapper.set("m.a", 2.50)
        assertEquals(mapOf("a" to "2.5", "b" to "007"), wrapper.getMap("m"))
    }

    @Test
    fun `getMap skips an entry replaced by a section`() {
        val (wrapper, warnings) = wrapperWithWarnings("m:\n  a: 1.10\n  b: 007\n")
        wrapper.set("m.a.inner", "x")
        assertEquals(mapOf("b" to "007"), wrapper.getMap("m"))
        assertEquals(listOf("Config value at 'm.a' is not a valid String (found a section), skipping"), warnings)
    }

    @Test
    fun `getString returns a programmatically set value as before`() {
        val wrapper = wrapperWith("value: 1.10")
        wrapper.addDefault("value", "")
        wrapper.set("value", 2.50)
        assertEquals("2.5", wrapper.getString("value"))
    }

    @Test
    fun `numeric getters parse values kept as typed`() {
        val wrapper = wrapperWith("value: 007")
        wrapper.addDefault("value", 0)
        assertEquals(7, wrapper.getInt("value"))
    }

    // ========================= Coercion of bad input =========================

    @Test
    fun `getString with list warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value:\n- a\n- b")
        wrapper.addDefault("value", "fallback")
        assertEquals("fallback", wrapper.getString("value"))
        assertEquals(listOf("Config value at 'value' is not a valid String (found a list), using the default (fallback)"), warnings)
    }

    @Test
    fun `getString with map warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value:\n  a: b")
        wrapper.addDefault("value", "fallback")
        assertEquals("fallback", wrapper.getString("value"))
        assertEquals(listOf("Config value at 'value' is not a valid String (found a section), using the default (fallback)"), warnings)
    }

    @Test
    fun `getStringList skips nested lists and maps with warning`() {
        val (wrapper, warnings) = wrapperWithWarnings("values:\n- a\n- [b, c]\n- {d: e}\n- f")
        assertEquals(listOf("a", "f"), wrapper.getStringList("values"))
        assertEquals(
            listOf(
                "Config list element at 'values' is not a valid String (found a list), skipping",
                "Config list element at 'values' is not a valid String (found a section), skipping"
            ),
            warnings
        )
    }

    @Test
    fun `getMap skips list values with warning`() {
        val (wrapper, warnings) = wrapperWithWarnings("map:\n  a: x\n  b: [1, 2]")
        assertEquals(mapOf("a" to "x"), wrapper.getMap("map"))
        assertEquals(listOf("Config value at 'map.b' is not a valid String (found a list), skipping"), warnings)
    }

    @Test
    fun `getMap skips empty values with warning`() {
        val (wrapper, warnings) = wrapperWithWarnings("map:\n  a: x\n  b:\n  c: ~")
        assertEquals(mapOf("a" to "x"), wrapper.getMap("map"))
        assertEquals(
            listOf(
                "Config value at 'map.b' is not a valid String (found empty), skipping",
                "Config value at 'map.c' is not a valid String (found empty), skipping"
            ),
            warnings
        )
    }

    @Test
    fun `getMap warns about nested sections`() {
        val (wrapper, warnings) = wrapperWithWarnings("map:\n  leaf: v\n  nested:\n    deep: v")
        assertEquals(mapOf("leaf" to "v"), wrapper.getMap("map"))
        assertEquals(listOf("Config value at 'map.nested' is not a valid String (found a section), skipping"), warnings)
    }

    @Test
    fun `getMap with a single value warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("map: x")
        wrapper.addDefault("map", mapOf("a" to "b"))
        assertEquals(mapOf("a" to "b"), wrapper.getMap("map"))
        assertEquals(listOf("Config value at 'map' is not a section (found 'x'), using the default ({a=b})"), warnings)
    }

    @Test
    fun `getLong with double above range warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: 1e19")
        wrapper.addDefault("value", 5L)
        assertEquals(5L, wrapper.getLong("value"))
        assertEquals(1, warnings.size)
    }

    @Test
    fun `getLong with double below range warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("value: -1e30")
        wrapper.addDefault("value", 5L)
        assertEquals(5L, wrapper.getLong("value"))
        assertEquals(1, warnings.size)
    }

    @Test
    fun `getLong with double at the range limits`() {
        val (wrapper, warnings) = wrapperWithWarnings("min: -9.223372036854775808E18\nmax: 9.223372036854775807E18")
        wrapper.addDefault("min", 5L)
        wrapper.addDefault("max", 5L)
        assertEquals(Long.MIN_VALUE, wrapper.getLong("min"))
        assertEquals(5L, wrapper.getLong("max"), "Long.MAX_VALUE is not exactly representable as a double")
        assertEquals(1, warnings.size)
    }

    @Test
    fun `getLongList skips doubles out of range`() {
        val (wrapper, warnings) = wrapperWithWarnings("values: [1, 1e19, 2.0]")
        assertEquals(listOf(1L, 2L), wrapper.getLongList("values"))
        assertEquals(listOf("Config list element at 'values' is not a valid Long (found '1e19'), skipping"), warnings)
    }

    @Test
    fun `warnings show values as typed in the file`() {
        val (wrapper, warnings) = wrapperWithWarnings(
            "ratio: 1.50\nflag: True\nvalues: [1, 1e19, 0.50]\nsingle: 1.50\nspring: True\ndup: 1.50\ndup: 2\n"
        )
        wrapper.getInt("ratio")
        wrapper.getInt("flag")
        wrapper.getLongList("values")
        wrapper.getIntegerList("single")
        wrapper.getBoolean("spring.enabled")
        assertEquals(
            listOf(
                "Config value at 'dup' is defined more than once, skipping the earlier one ('1.50')",
                "Config value at 'ratio' is not a valid Int (found '1.50'), using the default (0)",
                "Config value at 'flag' is not a valid Int (found 'True'), using the default (0)",
                "Config list element at 'values' is not a valid Long (found '1e19'), skipping",
                "Config list element at 'values' is not a valid Long (found '0.50'), skipping",
                "Config value at 'single' is not a list (found '1.50'), using the default ([])",
                "Config value at 'spring' is not a section (found 'True'), so 'spring.enabled' cannot be read from it, " +
                    "using the default"
            ),
            warnings
        )
    }

    @Test
    fun `warnings show values that are not from the file as converted`() {
        val (wrapper, warnings) = wrapperWithWarnings("ratio: 1.10\nempty: ~\n")
        wrapper.set("ratio", 1.50)
        wrapper.addDefault("empty", "abc")
        wrapper.getInt("ratio")
        wrapper.getInt("empty")
        assertEquals(
            listOf(
                "Config value at 'ratio' is not a valid Int (found '1.5'), using the default (0)",
                "Config value at 'empty' is not a valid Int (found 'abc'), using the default (0)"
            ),
            warnings
        )
    }

    @Test
    fun `file that is a single value is described as typed`() {
        val (wrapper, warnings) = wrapperWithWarnings("True")
        wrapper.addDefault("a", "default")
        assertEquals("default", wrapper.getString("a"))
        assertTrue(
            warnings.single().endsWith(".yml' does not contain keys and values (found 'True'), using the defaults"),
            "Unexpected warning: $warnings"
        )
    }

    @Test
    fun `an empty default is shown as empty in warnings`() {
        val (wrapper, warnings) = wrapperWithWarnings("prefix: [a]\nunregistered: [a]\nspring: true\n")
        wrapper.addDefault("prefix", "")
        wrapper.addDefault("spring.prefix", "")
        wrapper.getString("prefix")
        wrapper.getString("unregistered")
        wrapper.getString("spring.prefix")
        assertEquals(
            listOf(
                "Config value at 'prefix' is not a valid String (found a list), using the default (empty)",
                "Config value at 'unregistered' is not a valid String (found a list), using the default (empty)",
                "Config value at 'spring' is not a section (found 'true'), so 'spring.prefix' cannot be read from it, " +
                    "using the default (empty)"
            ),
            warnings
        )
    }

    @Test
    fun `getDouble rejects values that are not finite`() {
        for (value in listOf("NaN", "Infinity", "-Infinity", "'NaN'", ".inf", "-.inf", ".nan", "1e400")) {
            val (wrapper, warnings) = wrapperWithWarnings("value: $value")
            wrapper.addDefault("value", 1.5)
            assertEquals(1.5, wrapper.getDouble("value"), "for $value")
            assertEquals(1, warnings.size, "for $value")
        }
    }

    @Test
    fun `getDoubleList skips values that are not finite`() {
        val (wrapper, warnings) = wrapperWithWarnings("values: [1.5, NaN, Infinity, 1e400, 2.5]")
        assertEquals(listOf(1.5, 2.5), wrapper.getDoubleList("values"))
        assertEquals(3, warnings.size)
    }

    @Test
    fun `list elements without a value are skipped with a warning`() {
        val (wrapper, warnings) = wrapperWithWarnings("values:\n- 1\n-\n- 2")
        assertEquals(listOf(1, 2), wrapper.getIntegerList("values"))
        assertEquals(listOf("Config list element at 'values' is not a valid Int (found empty), skipping"), warnings)
    }

    @Test
    fun `reading below a scalar warns and returns default`() {
        val (wrapper, warnings) = wrapperWithWarnings("spring: true")
        wrapper.addDefault("spring.enabled", false)
        assertEquals(false, wrapper.getBoolean("spring.enabled"))
        assertEquals(
            listOf(
                "Config value at 'spring' is not a section (found 'true'), so 'spring.enabled' cannot be read from it, " +
                    "using the default (false)"
            ),
            warnings
        )
    }

    @Test
    fun `duplicate keys warn and the last one wins`() {
        val (wrapper, warnings) = wrapperWithWarnings("dup: 1\nother: x\ndup: 2")
        wrapper.addDefault("dup", 0)
        assertEquals(2, wrapper.getInt("dup"))
        assertEquals(listOf("Config value at 'dup' is defined more than once, skipping the earlier one ('1')"), warnings)
    }

    @Test
    fun `file that is not a section is ignored with a warning`() {
        val (wrapper, warnings) = wrapperWithWarnings("- a\n- b")
        wrapper.addDefault("a", "default")
        assertEquals("default", wrapper.getString("a"))
        assertEquals(1, warnings.size)
        assertTrue(
            warnings.single().endsWith(".yml' does not contain keys and values (found a list), using the defaults"),
            "Unexpected warning: $warnings"
        )
    }

    @Test
    fun `duplicate keys keep only the effective one on save`() {
        val file = dir.resolve("duplicate.yml")
        Files.writeString(file, "dup: 1\nother: x\ndup: 2\n")
        wrapConfig(file).save()
        assertEquals("dup: 2\nother: x\n", Files.readString(file))
    }

    @Test
    fun `list containing itself throws ConfigException`() {
        val file = dir.resolve("recursive-list.yml")
        Files.writeString(file, "a: &x [*x]")
        val exception = assertFailsWith<ConfigException> { wrapConfig(file) }
        assertTrue(exception.message!!.contains("'a'"), "Message should name the path: ${exception.message}")
    }

    @Test
    fun `map containing itself throws ConfigException`() {
        val file = dir.resolve("recursive-map.yml")
        Files.writeString(file, "a: &x {b: *x}")
        assertFailsWith<ConfigException> { wrapConfig(file) }
    }

    @Test
    fun `aliases without cycles are allowed`() {
        val wrapper = wrapperWith("base: &b {x: 1}\ncopy: *b\nlist: [*b, *b]")
        wrapper.addDefault("copy.x", 0)
        assertEquals(1, wrapper.getInt("copy.x"))
    }

    // ========================= Unreadable files =========================

    @Test
    fun `file that is not UTF-8 throws ConfigException`() {
        val file = dir.resolve("latin1.yml")
        Files.write(file, "name: Müller §\n".toByteArray(Charsets.ISO_8859_1))

        val exception = assertFailsWith<ConfigException> { wrapConfig(file) }
        assertTrue(exception.message!!.contains(file.toString()), "Message should contain file path")
        assertTrue(exception.message!!.contains("UTF-8"), "Message should ask for UTF-8: ${exception.message}")
    }

    @Test
    fun `directory instead of file throws ConfigException`() {
        val file = dir.resolve("directory.yml")
        Files.createDirectory(file)

        val exception = assertFailsWith<ConfigException> { wrapConfig(file) }
        assertTrue(exception.message!!.contains(file.toString()), "Message should contain file path")
    }

    @Test
    fun `file without read permission throws ConfigException`() {
        val file = dir.resolve("unreadable.yml")
        Files.writeString(file, "key: value")
        Files.setPosixFilePermissions(file, emptySet())
        assumeFalse(Files.isReadable(file), "File is still readable, e.g. when running as root")

        val exception = assertFailsWith<ConfigException> { wrapConfig(file) }
        assertTrue(exception.message!!.contains(file.toString()), "Message should contain file path")
    }

    @Test
    fun `reload of a file that is not UTF-8 keeps old data`() {
        val file = dir.resolve("reload-latin1.yml")
        Files.writeString(file, "key: original")
        val wrapper = wrapConfig(file)
        wrapper.addDefault("key", "")

        Files.write(file, "key: Müller\n".toByteArray(Charsets.ISO_8859_1))

        assertFailsWith<ConfigException> { wrapper.reloadConfig() }
        assertEquals("original", wrapper.getString("key"))
    }

    // ========================= Blank list or map =========================

    @Test
    fun `blank list reads as empty list`() {
        val wrapper = wrapperWith("items:\nnumbers: ~")
        wrapper.addDefault("items", listOf("a"))
        wrapper.addDefault("numbers", listOf(1))
        assertEquals(emptyList(), wrapper.getStringList("items"))
        assertEquals(emptyList(), wrapper.getIntegerList("numbers"))
    }

    @Test
    fun `blank map reads as empty map`() {
        val wrapper = wrapperWith("map:")
        wrapper.addDefault("map", mapOf("a" to "b"))
        assertEquals(emptyMap(), wrapper.getMap("map"))
    }

    @Test
    fun `missing list and map use defaults`() {
        val wrapper = wrapperWith("other: x")
        wrapper.addDefault("items", listOf("a"))
        wrapper.addDefault("map", mapOf("a" to "b"))
        assertEquals(listOf("a"), wrapper.getStringList("items"))
        assertEquals(mapOf("a" to "b"), wrapper.getMap("map"))
    }

    @Test
    fun `saveDefaults writes empty list and map for blank keys`() {
        val file = dir.resolve("blank.yml")
        Files.writeString(file, "# Worlds\nitems: # none yet\nmap:\nname:\n")
        val config = object : AbstractConfig(wrapConfig(file)) {
            val items by config("items", listOf("a"))
            val map by config("map", mapOf("k" to "v"))
            val name by config("name", "Steve")
        }

        config.saveDefaults()

        assertEquals("# Worlds\nitems: [] # none yet\nmap: {}\nname: Steve\n", Files.readString(file))
        assertEquals(emptyList(), config.items)
        assertEquals(emptyMap(), config.map)
    }

    // ========================= Round trip =========================

    @Test
    fun `strings with line breaks load back unchanged`() {
        for (value in listOf("line1\n\u0085line2", "a\nb\r\nc\u0085d e f", "line1\nline2\n")) {
            val file = dir.resolve("config-${System.nanoTime()}.yml")
            val wrapper = wrapConfig(file)
            wrapper.set("value", value)
            wrapper.save()

            val reloaded = wrapConfig(file)
            reloaded.addDefault("value", "")
            assertEquals(value, reloaded.getString("value"), "Written as:\n${Files.readString(file)}")
        }
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
