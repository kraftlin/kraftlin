package de.devsylum.kconfig.bukkit

import de.devsylum.kconfig.AbstractConfig
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BukkitFileConfigurationTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun `integration test for arbitrary config files`() {

        val configFile = directory.resolve("config.yml")

        // Declare config with default values using config with custom path
        val config = object : AbstractConfig(wrapConfig(configFile)) {

            var setting1: String by config("setting1", "Value 1")

            val setting2: Int by config("config.setting2", 10)

            var setting3: List<Boolean> by config("setting3", listOf(true, false, true))
        }

        assertEquals("Value 1", config.setting1)
        assertEquals(10, config.setting2)
        assertEquals(listOf(true, false, true), config.setting3)

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
            |""".trimMargin()

        val actual = Files.newBufferedReader(configFile).use {
            it.readText()
        }
        assertEquals(expected, actual)

        // Edit config file
        @Language("yaml")
        val editedConfig =
            """
            |setting1: new Value
            |config:
            |  setting2: 11
            |setting3: []
            |""".trimMargin()

        Files.newBufferedWriter(configFile).use {
            it.write(editedConfig)
        }

        // Reload config values
        config.reloadConfig()

        assertEquals("new Value", config.setting1)
        assertEquals(11, config.setting2)
        assertEquals(emptyList(), config.setting3)

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
            |""".trimMargin()

        val actual2 = Files.newBufferedReader(configFile).use {
            it.readText()
        }
        assertEquals(expected2, actual2)
    }
}
