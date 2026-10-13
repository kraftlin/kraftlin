package io.github.kraftlin.config

import io.github.kraftlin.config.AbstractConfig.ConfigWrapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.temporal.ChronoField
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DelegateFallbackTest {

    @TempDir
    lateinit var directory: Path

    // ========================= Custom types =========================

    @Test
    fun `unreadable custom value falls back to default with a warning`() {
        val configWrapper = mockWrapper {
            every { getString("value") } returns "bad"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: TestClass by config("value", TestClass("default"), TestClass::value, ::parseTestClass)
        }

        assertEquals(TestClass("default"), config.configValue)
        verify(exactly = 1) {
            configWrapper.warn("Config value at 'value' could not be read ('bad': not a test value), using default")
        }
    }

    @Test
    fun `warning names the exception type when it has no message`() {
        val configWrapper = mockWrapper {
            every { getString("value") } returns "bad"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: TestClass by config(
                "value", TestClass("default"),
                serialize = TestClass::value,
                deserialize = { throw IllegalStateException() }
            )
        }

        assertEquals(TestClass("default"), config.configValue)
        verify(exactly = 1) {
            configWrapper.warn("Config value at 'value' could not be read ('bad': java.lang.IllegalStateException), using default")
        }
    }

    @Test
    fun `warning names the exception type when the message is blank or the type is anonymous`() {
        val configWrapper = mockWrapper {
            every { getString(any()) } returns "bad"
        }

        val config = object : AbstractConfig(configWrapper) {
            val blankMessage: TestClass by config(
                "blank", TestClass("default"),
                serialize = TestClass::value,
                deserialize = { throw IllegalArgumentException(" ") }
            )
            val anonymous: TestClass by config(
                "anonymous", TestClass("default"),
                serialize = TestClass::value,
                deserialize = { throw object : RuntimeException() {} }
            )
        }

        config.blankMessage
        config.anonymous
        verify(exactly = 1) {
            configWrapper.warn("Config value at 'blank' could not be read ('bad': java.lang.IllegalArgumentException), using default")
        }
        verify(exactly = 1) {
            configWrapper.warn(match {
                it.startsWith("Config value at 'anonymous' could not be read ('bad': io.github.kraftlin.config.")
            })
        }
    }

    @Test
    fun `unreadable custom value fallback is cached`() {
        val configWrapper = mockWrapper {
            every { getString("value") } returns "bad"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: TestClass by config("value", TestClass("default"), TestClass::value, ::parseTestClass)
        }

        config.configValue
        assertEquals(TestClass("default"), config.configValue)
        verify(exactly = 1) { configWrapper.getString("value") }
        verify(exactly = 1) { configWrapper.warn(any()) }
    }

    @Test
    fun `unreadable custom list elements are skipped with a warning`() {
        val configWrapper = mockWrapper {
            every { getStringList("value") } returns listOf("first", "bad", "second")
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: List<TestClass> by config(
                "value", listOf(TestClass("default")), TestClass::value, ::parseTestClass
            )
        }

        assertEquals(listOf(TestClass("first"), TestClass("second")), config.configValue)
        verify(exactly = 1) {
            configWrapper.warn("Config list element at 'value' could not be read ('bad': not a test value), skipping")
        }
    }

    @Test
    fun `unreadable custom map entries are skipped with a warning`() {
        val configWrapper = mockWrapper {
            every { getMap("value") } returns mapOf("key1" to "first", "key2" to "bad", "key3" to "third")
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: Map<String, TestClass> by config(
                "value", mapOf("key" to TestClass("default")), TestClass::value, ::parseTestClass
            )
        }

        assertEquals(mapOf("key1" to TestClass("first"), "key3" to TestClass("third")), config.configValue)
        assertEquals(listOf("key1", "key3"), config.configValue.keys.toList())
        verify(exactly = 1) {
            configWrapper.warn("Config value at 'value.key2' could not be read ('bad': not a test value), skipping")
        }
    }

    @Test
    fun `custom list and map values stay mutable like the built-in list delegates`() {
        val configWrapper = mockWrapper {
            every { getStringList("list") } returns listOf("first")
            every { getMap("map") } returns mapOf("key1" to "first")
        }

        val config = object : AbstractConfig(configWrapper) {
            val list: List<TestClass> by config("list", listOf(TestClass("default")), TestClass::value, ::parseTestClass)
            val map: Map<String, TestClass> by config(
                "map", mapOf("key" to TestClass("default")), TestClass::value, ::parseTestClass
            )
        }

        (config.list as MutableList<TestClass>).add(TestClass("second"))
        (config.map as MutableMap<String, TestClass>)["key2"] = TestClass("second")

        assertEquals(listOf(TestClass("first"), TestClass("second")), config.list)
        assertEquals(mapOf("key1" to TestClass("first"), "key2" to TestClass("second")), config.map)
    }

    // ========================= Enums =========================

    @Test
    fun `enum value matches pascal case entries case-insensitively`() {
        val configWrapper = mockWrapper {
            every { getString("value") } returns "yaml"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: Format by config("value", Format.Json)
        }

        assertEquals(Format.Yaml, config.configValue)
        verify { configWrapper.addDefault("value", "json") }
        verify(exactly = 0) { configWrapper.warn(any()) }
    }

    @Test
    fun `enum value is trimmed and accepts dashes`() {
        val configWrapper = mockWrapper {
            every { getString("value") } returns " Foo-Bar "
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: TestEnum by config("value", TestEnum.FOO)
        }

        assertEquals(TestEnum.FOO_BAR, config.configValue)
    }

    @Test
    fun `unknown enum value falls back to default listing valid values`() {
        val configWrapper = mockWrapper {
            every { getString("value") } returns "xml"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: Format by config("value", Format.Json)
        }

        assertEquals(Format.Json, config.configValue)
        verify(exactly = 1) {
            configWrapper.warn("Config value at 'value' could not be read ('xml': expected one of json, yaml), using default")
        }
    }

    @Test
    fun `unknown value of a large enum names the type instead of listing all values`() {
        val configWrapper = mockWrapper {
            every { getString("value") } returns "yeer"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: ChronoField by config("value", ChronoField.YEAR)
        }

        assertEquals(ChronoField.YEAR, config.configValue)
        verify(exactly = 1) {
            configWrapper.warn("Config value at 'value' could not be read ('yeer': expected a valid ChronoField name), using default")
        }
    }

    @Test
    fun `enum entry names containing dashes read back their own written default`() {
        val configWrapper = mockWrapper {
            every { getString("value") } returns "foo-bar"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: DashedEnum by config("value", DashedEnum.`foo-bar`)
        }

        assertEquals(DashedEnum.`foo-bar`, config.configValue)
        verify { configWrapper.addDefault("value", "foo-bar") }
        verify(exactly = 0) { configWrapper.warn(any()) }
    }

    @Test
    fun `enum list matches pascal case entries and skips unknown names`() {
        val configWrapper = mockWrapper {
            every { getStringList("value") } returns listOf("json", "xml", "YAML")
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: List<Format> by config("value", listOf(Format.Json))
        }

        assertEquals(listOf(Format.Json, Format.Yaml), config.configValue)
        verify(exactly = 1) {
            configWrapper.warn("Config list element at 'value' could not be read ('xml': expected one of json, yaml), skipping")
        }
    }

    @Test
    fun `pascal case enum reads back its own written default`() {
        val configFile = directory.resolve("config.yml")
        val config = object : AbstractConfig(wrapConfig(configFile)) {
            val format: Format by config("format", Format.Yaml)
            val formats: List<Format> by config("formats", listOf(Format.Json, Format.Yaml))
        }

        config.saveDefaults()

        assertTrue(Files.readString(configFile).contains("format: yaml"), "Default should be written lowercased")
        assertEquals(Format.Yaml, config.format)
        assertEquals(listOf(Format.Json, Format.Yaml), config.formats)
    }

    // ========================= UUIDs =========================

    @Test
    fun `invalid uuid falls back to default with a warning`() {
        val defaultId = UUID.nameUUIDFromBytes("defaultId".toByteArray())
        val configWrapper = mockWrapper {
            every { getString("value") } returns "not-a-uuid"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: UUID by config("value", defaultId)
        }

        assertEquals(defaultId, config.configValue)
        verify(exactly = 1) {
            configWrapper.warn(match { it.startsWith("Config value at 'value' could not be read ('not-a-uuid': ") })
        }
    }

    @Test
    fun `short uuid forms are still accepted`() {
        val configWrapper = mockWrapper {
            every { getString("value") } returns "0-0-0-0-1"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: UUID by config("value", UUID.randomUUID())
        }

        assertEquals(UUID(0, 1), config.configValue)
        verify(exactly = 0) { configWrapper.warn(any()) }
    }

    @Test
    fun `invalid uuid list elements are skipped with a warning`() {
        val id = UUID.nameUUIDFromBytes("id".toByteArray())
        val configWrapper = mockWrapper {
            every { getStringList("value") } returns listOf(id.toString(), "not-a-uuid", "0-0-0-0-1")
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: List<UUID> by config("value", listOf(id))
        }

        assertEquals(listOf(id, UUID(0, 1)), config.configValue)
        verify(exactly = 1) {
            configWrapper.warn(match { it.startsWith("Config list element at 'value' could not be read ('not-a-uuid': ") })
        }
    }

    enum class Format {
        Json,
        Yaml
    }

    enum class TestEnum {
        FOO,
        FOO_BAR
    }

    @Suppress("EnumEntryName")
    enum class DashedEnum {
        `foo-bar`,
        baz
    }

    data class TestClass(val value: String)

    private fun parseTestClass(value: String): TestClass {
        require(value != "bad") { "not a test value" }
        return TestClass(value)
    }

    private fun mockWrapper(block: ConfigWrapper.() -> Unit = {}): ConfigWrapper {
        val wrapper = mockk<ConfigWrapper>(relaxed = true)
        wrapper.block()
        return wrapper
    }
}
