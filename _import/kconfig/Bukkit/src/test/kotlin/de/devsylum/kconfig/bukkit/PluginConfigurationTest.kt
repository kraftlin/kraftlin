package de.devsylum.kconfig.bukkit

import com.google.common.base.Charsets
import de.devsylum.kconfig.AbstractConfig
import io.mockk.every
import io.mockk.mockk
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import org.intellij.lang.annotations.Language
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.test.assertEquals

internal class PluginConfigurationTest {

    @get:Rule
    val directory = TemporaryFolder()

    @Test
    fun `integration test for arbitrary config files`() {

        val configFile = directory.newFile().toPath()
        val plugin = mockPlugin(configFile.toFile())

        // Declare config with default values using default plugin config
        val config = object : AbstractConfig(wrapConfig(plugin)) {

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
        assertEquals(expected, actual)

        // Edit config file
        @Language("yaml")
        val editedConfig =
            """
            |setting1: new Value
            |setting3: []
            |setting4:
            |  new: value
            |  number: 1.1
            |config:
            |  setting2: 11
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
            |setting3:
            |- false
            |- false
            |setting4:
            |  new: value
            |  number: 1.1
            |config:
            |  setting2: 11
            |""".trimMargin()

        val actual2 = Files.newBufferedReader(configFile).use {
            it.readText()
        }
        assertEquals(expected2, actual2)
    }
}

private fun mockPlugin(configFile: File): Plugin {

    var newConfig: FileConfiguration? = null

    // mocked methods behave like those of the JavaPlugin implementation
    return mockk<Plugin>().apply {

        every { config } answers {
            if (newConfig == null) {
                reloadConfig()
            }
            newConfig!!
        }

        every { reloadConfig() } answers {
            newConfig = YamlConfiguration.loadConfiguration(configFile)
            val defConfigStream = getResource("config.yml")
            if (defConfigStream != null) {
                newConfig!!.setDefaults(
                    YamlConfiguration.loadConfiguration(
                        InputStreamReader(
                            defConfigStream,
                            Charsets.UTF_8
                        )
                    )
                )
            }
        }

        every { getResource(any()) } returns null

        every { saveConfig() } answers {
            try {
                config.save(configFile)
            } catch (ex: IOException) {
                Logger.getAnonymousLogger().log(Level.SEVERE, "Could not save config to $configFile", ex)
            }
        }
    }
}
