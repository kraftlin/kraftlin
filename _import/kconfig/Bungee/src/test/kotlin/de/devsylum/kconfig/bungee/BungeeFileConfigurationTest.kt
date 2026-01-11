package de.devsylum.kconfig.bungee

import de.devsylum.kconfig.AbstractConfig
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BungeeFileConfigurationTest {

    @get:Rule
    val directory = TemporaryFolder()

    @Test
    fun `integration test for arbitrary config files`() {

        val configFile = directory.newFile("config.yml").toPath()

        // Declare config with default values using config with custom path
        val config = object : AbstractConfig(wrapConfig(configFile)) {

            var setting1: String by config("setting1", "Value 1")

            val setting2: Int by config("config.setting2", 10)

            var setting3: List<Boolean> by config("setting3", listOf(true, false, true))

            val setting4: Map<String, String> by config(
                "setting4",
                mapOf("key1" to "value1", "key2" to "value2", "key3" to "5")
            )
        }

        assertEquals("Value 1", config.setting1)
        assertEquals(10, config.setting2)
        assertEquals(listOf(true, false, true), config.setting3)
        assertEquals(mapOf("key1" to "value1", "key2" to "value2", "key3" to "5"), config.setting4)

        // Store default values to disk
        config.saveDefaults()

        @Language("yaml")
        val expected =
            """
            |setting1: Value 1
            |config:
            |  setting2: 10
            |setting3:
            |- true
            |- false
            |- true
            |setting4:
            |  key1: value1
            |  key2: value2
            |  key3: '5'
            |""".trimMargin()

        val actual = Files.newBufferedReader(configFile).use {
            it.readText()
        }
        assertEquals(expected.split('\n').toSet(), actual.split('\n').toSet())

        // Edit config file
        @Language("yaml")
        val editedConfig =
            """
            |setting1: new Value
            |config:
            |  setting2: 11
            |setting3: []
            |setting4:
            |  new: value
            |  number: 1.1
            |""".trimMargin()

        Files.newBufferedWriter(configFile).use {
            it.write(editedConfig)
        }

        // Reload config values
        config.reloadConfig()

        assertEquals("new Value", config.setting1)
        assertEquals(11, config.setting2)
        assertEquals(emptyList(), config.setting3)
        assertEquals(mapOf("new" to "value", "number" to "1.1"), config.setting4)

        config.setting1 = "Third Value"
        config.setting3 = listOf(false, false)
        config.save()

        assertEquals(config.setting1, "Third Value")
        assertEquals(config.setting3, listOf(false, false))

        @Language("yaml")
        val expected2 =
            """
            |setting1: Third Value
            |config:
            |  setting2: 11
            |setting3:
            |- false
            |- false
            |setting4:
            |  new: value
            |  number: 1.1
            |""".trimMargin()

        val actual2 = Files.newBufferedReader(configFile).use {
            it.readText()
        }
        assertEquals(expected2, actual2)
    }

    @Test
    fun `defaults do not override customized values`() {

        val configFile = directory.newFile().toPath()

        @Language("yaml")
        val editedConfig =
            """
            |setting1: new Value
            |config:
            |  setting2: 10000
            |""".trimMargin()

        Files.newBufferedWriter(configFile).use { it.write(editedConfig) }

        val config = object : AbstractConfig(wrapConfig(configFile)) {

            val setting1: String by config("setting1", "default Value")

            val setting2: Int by config("config.setting2", 10)
        }

        config.saveDefaults()
        config.reloadConfig()

        assertEquals(config.setting1, "new Value")
        assertEquals(config.setting2, 10000)
    }

    @Test
    fun `load config from non exiting path`() {
        val configFile = directory.root.toPath().resolve("parent/not-existing-file.yml")

        // Declare config with default values using config with custom path
        val config = object : AbstractConfig(wrapConfig(configFile)) {
            val setting: String by config("setting", "value")
        }

        assertEquals("value", config.setting)
        config.saveDefaults()
    }
}
