package io.github.kraftlin.config.paper

import io.github.kraftlin.config.AbstractConfig
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.nio.file.Path

/**
 * Wraps the default plugin config provided by [Plugin.getConfig] for use in [AbstractConfig].
 * This is the recommended approach when using a single configuration file only.
 *
 * @param plugin The plugin instance to which the config belongs.
 */
public fun wrapConfig(plugin: Plugin): AbstractConfig.ConfigWrapper = object : AbstractConfig.ConfigWrapper {

    val config: Configuration get() = plugin.config

    override fun addDefault(path: String, value: Any) = config.addDefault(path, value)

    override fun set(path: String, value: Any?) = config.set(path, value)

    override fun getBoolean(path: String) = config.getBoolean(path)

    override fun getInt(path: String) = config.getInt(path)

    override fun getLong(path: String) = config.getLong(path)

    override fun getDouble(path: String) = config.getDouble(path)

    override fun getString(path: String) = config.getString(path)!!

    override fun getBooleanList(path: String) = config.getBooleanList(path)

    override fun getIntegerList(path: String) = config.getIntegerList(path)

    override fun getLongList(path: String) = config.getLongList(path)

    override fun getDoubleList(path: String) = config.getDoubleList(path)

    override fun getStringList(path: String) = config.getStringList(path)

    override fun getMap(path: String): Map<String, Any> {
        return if (config.isConfigurationSection(path)) {
            config.getConfigurationSection(path)!!.getValues(false)
        } else {
            //Workaround, as addDefault() with maps does not properly initiate a new ConfigurationSection
            @Suppress("UNCHECKED_CAST")
            config.get(path) as? Map<String, Any> ?: emptyMap()
        }
    }

    override fun reloadConfig() {
        plugin.reloadConfig()
    }

    override fun saveDefaults() {
        config.options().copyDefaults(true)
        plugin.saveConfig()
    }

    override fun save() {
        plugin.saveConfig()
    }
}

/**
 * Wraps an arbitrary `YAML` file for use in [AbstractConfig]. This way, a plugin can support multiple configurations.
 */
public fun wrapConfig(configPath: Path): AbstractConfig.ConfigWrapper = object : AbstractConfig.ConfigWrapper {

    var config: YamlConfiguration = YamlConfiguration.loadConfiguration(configPath.toFile())

    override fun addDefault(path: String, value: Any) = config.addDefault(path, value)

    override fun set(path: String, value: Any?) = config.set(path, value)

    override fun getBoolean(path: String) = config.getBoolean(path)

    override fun getInt(path: String) = config.getInt(path)

    override fun getLong(path: String) = config.getLong(path)

    override fun getDouble(path: String) = config.getDouble(path)

    override fun getString(path: String) = config.getString(path)!!

    override fun getBooleanList(path: String) = config.getBooleanList(path)

    override fun getIntegerList(path: String) = config.getIntegerList(path)

    override fun getLongList(path: String) = config.getLongList(path)

    override fun getDoubleList(path: String) = config.getDoubleList(path)

    override fun getStringList(path: String) = config.getStringList(path)

    override fun getMap(path: String) = config.getConfigurationSection(path)!!.getValues(false)

    override fun reloadConfig() {
        config.load(configPath.toFile())
    }

    override fun saveDefaults() {
        config.options().copyDefaults(true)
        config.save(configPath.toFile())
    }

    override fun save() {
        config.save(configPath.toFile())
    }
}
