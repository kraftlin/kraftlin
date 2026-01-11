package io.github.kraftlin.config

import io.github.kraftlin.config.AbstractConfig.ConfigWrapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

internal class KConfigTest {

    @Test
    fun `read only boolean config value`() {
        val configWrapper = mockWrapper {
            every { getBoolean("value") } returns true
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: Boolean by config("value", true)
        }

        assertEquals(true, config.configValue)
        verify { configWrapper.addDefault("value", true) }
    }

    @Test
    fun `mutable boolean config value`() {
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: Boolean by config("value", true)
        }

        config.configValue = false

        verify { configWrapper.set("value", false) }
    }

    @Test
    fun `long config value`() {
        val configWrapper = mockWrapper {
            every { getLong("value") } returns 395L
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: Long by config("value", 10L)
        }

        assertEquals(395, config.configValue)
        verify { configWrapper.addDefault("value", 10L) }
    }

    @Test
    fun `mutable long config value`() {
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: Long by config("value", 10L)
        }

        config.configValue = 395L

        verify { configWrapper.set("value", 395L) }
    }

    @Test
    fun `int config value`() {
        val configWrapper = mockWrapper {
            every { getInt("value") } returns 395
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: Int by config("value", 10)
        }

        assertEquals(395, config.configValue)
        verify { configWrapper.addDefault("value", 10) }
    }

    @Test
    fun `mutable int config value`() {
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: Int by config("value", 10)
        }

        config.configValue = 395

        verify { configWrapper.set("value", 395) }
    }

    @Test
    fun `double config value`() {
        val configWrapper = mockWrapper {
            every { getDouble("value") } returns 62.554
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: Double by config("value", 10.994)
        }

        assertEquals(62.554, config.configValue)
        verify { configWrapper.addDefault("value", 10.994) }
    }

    @Test
    fun `mutable double config value`() {
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: Double by config("value", 10.994)
        }

        config.configValue = 62.554

        verify { configWrapper.set("value", 62.554) }
    }

    @Test
    fun `string config value`() {
        val configWrapper = mockWrapper {
            every { getString("value") } returns "Config Value"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: String by config("value", "Default Value")
        }

        assertEquals("Config Value", config.configValue)
        verify { configWrapper.addDefault("value", "Default Value") }
    }

    @Test
    fun `mutable string config value`() {
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: String by config("value", "Default Value")
        }

        config.configValue = "New Value"

        verify { configWrapper.set("value", "New Value") }
    }

    @Test
    fun `enum config value`() {
        val configWrapper = mockWrapper {
            every { getString("value") } returns "foo"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: TestEnum by config(
                "value",
                TestEnum.FOO_BAR
            )
        }

        assertEquals(TestEnum.FOO, config.configValue)
        verify { configWrapper.addDefault("value", "foo_bar") }
    }

    @Test
    fun `mutable enum config value`() {
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: TestEnum by config("value", TestEnum.FOO_BAR)
        }

        config.configValue = TestEnum.FOO

        verify { configWrapper.set("value", "foo") }
    }

    @Test
    fun `uuid config value`() {
        val testId = UUID.nameUUIDFromBytes("testId".toByteArray())
        val defaultId = UUID.nameUUIDFromBytes("defaultId".toByteArray())

        val configWrapper = mockWrapper {
            every { getString("value") } returns testId.toString()
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: UUID by config("value", defaultId)
        }

        assertEquals(testId, config.configValue)
        verify { configWrapper.addDefault("value", defaultId.toString()) }
    }

    @Test
    fun `mutable uuid config value`() {
        val testId = UUID.nameUUIDFromBytes("testId".toByteArray())
        val defaultId = UUID.nameUUIDFromBytes("defaultId".toByteArray())
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: UUID by config("value", defaultId)
        }

        config.configValue = testId

        verify { configWrapper.set("value", testId.toString()) }
    }

    @Test
    fun `generic config value`() {
        val testObject = TestClass("test")
        val defaultObject = TestClass("default")

        val configWrapper = mockWrapper {
            every { getString("value") } returns "test"
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: TestClass by config("value", defaultObject,
                serialize = TestClass::value,
                deserialize = { stringValue -> TestClass(stringValue) }
            )
        }

        assertEquals(testObject, config.configValue)
        verify { configWrapper.addDefault("value", "default") }
    }

    @Test
    fun `mutable generic config value`() {
        val testObject = TestClass("test")
        val defaultObject = TestClass("default")

        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: TestClass by config("value", defaultObject,
                serialize = TestClass::value,
                deserialize = { stringValue -> TestClass(stringValue) }
            )
        }

        config.configValue = testObject

        verify { configWrapper.set("value", "test") }
    }

    @Test
    fun `boolean list config value`() {
        val configWrapper = mockWrapper {
            every { getBooleanList("value") } returns listOf(true, true, false)
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: List<Boolean> by config("value", listOf(false))
        }

        assertEquals(listOf(true, true, false), config.configValue)
        verify { configWrapper.addDefault("value", listOf(false)) }
    }

    @Test
    fun `mutable boolean list config value`() {
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: List<Boolean> by config("value", listOf(false))
        }

        config.configValue = listOf(true, true, false)

        verify { configWrapper.set("value", listOf(true, true, false)) }
    }

    @Test
    fun `int list config value`() {
        val configWrapper = mockWrapper {
            every { getIntegerList("value") } returns listOf(1, 2, 3)
        }

        val config = object : AbstractConfig(configWrapper) {
            @Suppress("RemoveExplicitTypeArguments")
            val configValue: List<Int> by config("value", listOf<Int>(0))
        }

        assertEquals(listOf(1, 2, 3), config.configValue)
        verify { configWrapper.addDefault("value", listOf(0)) }
    }

    @Test
    fun `mutable int list config value`() {
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            @Suppress("RemoveExplicitTypeArguments")
            var configValue: List<Int> by config("value", listOf<Int>(0))
        }

        config.configValue = listOf(1, 2, 3)

        verify { configWrapper.set("value", listOf(1, 2, 3)) }
    }

    @Test
    fun `long list config value`() {
        val configWrapper = mockWrapper {
            every { getLongList("value") } returns listOf(1L, 2L, 3L)
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: List<Long> by config("value", listOf(0L))
        }

        assertEquals(listOf(1L, 2L, 3L), config.configValue)
        verify { configWrapper.addDefault("value", listOf(0L)) }
    }

    @Test
    fun `double list config value`() {
        val configWrapper = mockWrapper {
            every { getDoubleList("value") } returns listOf(1.1, 2.2, 3.3)
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: List<Double> by config("value", listOf(0.0))
        }

        assertEquals(listOf(1.1, 2.2, 3.3), config.configValue)
        verify { configWrapper.addDefault("value", listOf(0.0)) }
    }

    @Test
    fun `string list config value`() {
        val configWrapper = mockWrapper {
            every { getStringList("value") } returns listOf("abc", "D", "e")
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: List<String> by config("value", listOf("def"))
        }

        assertEquals(listOf("abc", "D", "e"), config.configValue)
        verify { configWrapper.addDefault("value", listOf("def")) }
    }

    @Test
    fun `enum list config value`() {
        val configWrapper = mockWrapper {
            every { getStringList("value") } returns listOf("foo", "foo-bar", "FOO_BAR")
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: List<TestEnum> by config("value", listOf(TestEnum.FOO))
        }

        assertEquals(
            listOf(
                TestEnum.FOO,
                TestEnum.FOO_BAR,
                TestEnum.FOO_BAR
            ), config.configValue
        )
        verify { configWrapper.addDefault("value", listOf("foo")) }
    }

    @Test
    fun `uuid list config value`() {
        val id1 = UUID.nameUUIDFromBytes("id1".toByteArray())
        val id2 = UUID.nameUUIDFromBytes("id2".toByteArray())
        val id3 = UUID.nameUUIDFromBytes("id3".toByteArray())

        val configWrapper = mockWrapper {
            every { getStringList("value") } returns listOf(id1.toString(), id2.toString(), id3.toString())
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: List<UUID> by config("value", listOf(id1, id2))
        }

        assertEquals(listOf(id1, id2, id3), config.configValue)
        verify { configWrapper.addDefault("value", listOf(id1.toString(), id2.toString())) }
    }

    @Test
    fun `generic list config value`() {
        val object1 = TestClass("object1")
        val object2 = TestClass("object2")
        val object3 = TestClass("object3")

        val configWrapper = mockWrapper {
            every { getStringList("value") } returns listOf("object3", "object2", "object3")
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: List<TestClass> by config("value", listOf(object2, object1),
                serialize = TestClass::value,
                deserialize = { stringValue -> TestClass(stringValue) }
            )
        }

        assertEquals(listOf(object3, object2, object3), config.configValue)
        verify { configWrapper.addDefault("value", listOf("object2", "object1")) }
    }

    @Test
    fun `mutable generic list config value`() {
        val object1 = TestClass("object1")
        val object2 = TestClass("object2")
        val object3 = TestClass("object3")
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: List<TestClass> by config("value", listOf(object2, object1),
                serialize = TestClass::value,
                deserialize = { stringValue -> TestClass(stringValue) }
            )
        }

        config.configValue = listOf(object3, object2, object3)

        verify { configWrapper.set("value", listOf("object3", "object2", "object3")) }
    }

    @Test
    fun `generic map config value`() {
        val object1 = TestClass("object1")
        val object2 = TestClass("object2")
        val object3 = TestClass("object3")

        val configWrapper = mockWrapper {
            every { getMap("value") } returns mapOf("key1" to "object3", "key2" to "object2", "key3" to "object3")
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: Map<String, TestClass> by config("value", mapOf("key2" to object2, "key1" to object1),
                serialize = TestClass::value,
                deserialize = { stringValue -> TestClass(stringValue) }
            )
        }

        assertEquals(mapOf("key1" to object3, "key2" to object2, "key3" to object3), config.configValue)
        verify { configWrapper.addDefault("value", mapOf("key2" to "object2", "key1" to "object1")) }
    }

    @Test
    fun `mutable generic map config value`() {
        val object1 = TestClass("object1")
        val object2 = TestClass("object2")
        val object3 = TestClass("object3")
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: Map<String, TestClass> by config("value", mapOf("key2" to object2, "key1" to object1),
                serialize = TestClass::value,
                deserialize = { stringValue -> TestClass(stringValue) }
            )
        }

        config.configValue = mapOf("key1" to object3, "key2" to object2, "key3" to object3)

        verify { configWrapper.set("value", mapOf("key1" to "object3", "key2" to "object2", "key3" to "object3")) }
    }

    @Test
    fun `string map config value`() {
        val configWrapper = mockWrapper {
            every { getMap("value") } returns mapOf("1" to "abc", "2" to "D", "3" to "e")
        }

        val config = object : AbstractConfig(configWrapper) {
            val configValue: Map<String, String> by config("value", mapOf("key" to "def"))
        }

        assertEquals(mapOf("1" to "abc", "2" to "D", "3" to "e"), config.configValue)
        verify { configWrapper.addDefault("value", mapOf("key" to "def")) }
    }

    @Test
    fun `mutable string map config value`() {
        val configWrapper = mockWrapper()

        val config = object : AbstractConfig(configWrapper) {
            var configValue: Map<String, String> by config("value", mapOf("key" to "def"))
        }

        config.configValue = mapOf("1" to "abc", "2" to "D", "3" to "e")

        verify { configWrapper.set("value", mapOf("1" to "abc", "2" to "D", "3" to "e")) }
    }

    enum class TestEnum {
        FOO,
        FOO_BAR
    }

    data class TestClass(val value: String)

    private fun mockWrapper(block: ConfigWrapper.() -> Unit = {}): ConfigWrapper {
        val wrapper = mockk<ConfigWrapper>(relaxed = true)
        wrapper.block()
        return wrapper
    }
}
