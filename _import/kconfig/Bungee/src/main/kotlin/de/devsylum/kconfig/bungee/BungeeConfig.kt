package de.devsylum.kconfig.bungee

import de.devsylum.kconfig.AbstractConfig
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.nio.file.Files
import java.nio.file.Path

/**
 * Wraps the default plugin config `config.yml` for use in [AbstractConfig].
 * This is the recommended approach when using a single configuration file only.
 *
 * @param plugin The plugin instance to which the config belongs.
 */
public fun wrapConfig(plugin: Plugin): AbstractConfig.ConfigWrapper =
    wrapConfig(plugin.dataFolder.toPath().resolve("config.yml"))

/**
 * Wraps an arbitrary `YAML` file for use in [AbstractConfig]. This way, a plugin can support multiple configurations.
 */
public fun wrapConfig(configPath: Path): AbstractConfig.ConfigWrapper = object : AbstractConfig.ConfigWrapper {

    private val provider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)

    val defaultConfig = Configuration()

    var config: Configuration = loadConfigOrDefault()

    override fun addDefault(path: String, value: Any) = defaultConfig.set(path, value)

    override fun set(path: String, value: Any?) = config.set(path, value)

    override fun getBoolean(path: String) = config.getBoolean(path)

    override fun getInt(path: String) = config.getInt(path)

    override fun getLong(path: String) = config.getLong(path)

    override fun getDouble(path: String) = config.getDouble(path)

    override fun getString(path: String) = config.getString(path)

    override fun getBooleanList(path: String) = config.getBooleanList(path)

    override fun getIntegerList(path: String) = config.getIntList(path)

    override fun getLongList(path: String) = config.getLongList(path)

    override fun getDoubleList(path: String) = config.getDoubleList(path)

    override fun getStringList(path: String) = config.getStringList(path)

    override fun getMap(path: String): Map<String, Any> {
        val section = config.getSection(path)
        return section.keys.associateWith { section[it] }
            .filter { (_, value) -> value !is Configuration }
    }

    override fun reloadConfig() {
        config = loadConfigOrDefault()
    }

    private fun loadConfigOrDefault(): Configuration {
        return if (Files.exists(configPath)) {
            provider.load(configPath.toFile(), defaultConfig)
        } else {
            Configuration(defaultConfig)
        }
    }

    override fun save() {
        provider.save(config, configPath.toFile())
    }

    override fun saveDefaults() {

        fun Configuration.traverseDefaults(keyPrefix: String? = null) {
            for (key in keys) {
                val value = get(key)
                val completePath = if (keyPrefix != null) "$keyPrefix.$key" else key
                if (!config.contains(completePath)) {
                    config.set(completePath, value)
                } else if (value is Configuration) {
                    value.traverseDefaults(completePath)
                }
            }
        }

        defaultConfig.traverseDefaults()
        val configDir = configPath.parent
        if (!Files.isDirectory(configDir)) {
            Files.createDirectories(configDir)
        }
        provider.save(config, configPath.toFile())
    }
}
