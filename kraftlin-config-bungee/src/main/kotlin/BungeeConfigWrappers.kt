package io.github.kraftlin.config.bungee

import io.github.kraftlin.config.AbstractConfig
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
 *
 * Note: BungeeCord's YAML API does not support comments. Any comments provided via config delegates will be silently
 * ignored.
 */
public fun wrapConfig(configPath: Path): AbstractConfig.ConfigWrapper = object : AbstractConfig.ConfigWrapper {

    private val provider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)

    private val defaultConfig = Configuration()

    private var config: Configuration = loadConfigOrDefault()

    override fun getKeys(deep: Boolean): Set<String> {
        return if (deep) {
            collectKeysDeep(config)
        } else {
            config.keys.toSet()
        }
    }

    private fun collectKeysDeep(section: Configuration, prefix: String? = null): Set<String> {
        val keys = mutableSetOf<String>()
        for (key in section.keys) {
            val fullPath = if (prefix != null) "$prefix.$key" else key
            val value = section.get(key)
            if (value is Configuration) {
                keys.addAll(collectKeysDeep(value, fullPath))
            } else {
                keys.add(fullPath)
            }
        }
        return keys
    }

    override fun remove(path: String) {
        config.set(path, null)
        // Clean up empty parent sections that BungeeCord leaves behind
        var current = path
        while ('.' in current) {
            current = current.substringBeforeLast('.')
            val section = config.getSection(current)
            if (section.keys.isEmpty()) {
                config.set(current, null)
            } else {
                break
            }
        }
    }

    override fun addDefault(path: String, value: Any) {
        defaultConfig.set(path, value)
    }

    override fun set(path: String, value: Any?) {
        config.set(path, value)
    }

    override fun getBoolean(path: String): Boolean = config.getBoolean(path)

    override fun getInt(path: String): Int = config.getInt(path)

    override fun getLong(path: String): Long = config.getLong(path)

    override fun getDouble(path: String): Double = config.getDouble(path)

    override fun getString(path: String): String = config.getString(path)

    override fun getBooleanList(path: String): List<Boolean> = config.getBooleanList(path)

    override fun getIntegerList(path: String): List<Int> = config.getIntList(path)

    override fun getLongList(path: String): List<Long> = config.getLongList(path)

    override fun getDoubleList(path: String): List<Double> = config.getDoubleList(path)

    override fun getStringList(path: String): List<String> = config.getStringList(path)

    override fun getMap(path: String): Map<String, Any> {
        val section = config.getSection(path)
        return section.keys.associateWith { section[it] }
            .filter { (_, value) -> value !is Configuration }
    }

    override fun reloadConfig() {
        config = loadConfigOrDefault()
    }

    override fun saveDefaults() {
        defaultConfig.traverseDefaults()
        val configDir = configPath.parent
        if (!Files.isDirectory(configDir)) {
            Files.createDirectories(configDir)
        }
        provider.save(config, configPath.toFile())
    }

    private fun Configuration.traverseDefaults(keyPrefix: String? = null) {
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

    override fun save() {
        provider.save(config, configPath.toFile())
    }

    override fun getComments(path: String): List<String> = emptyList()

    override fun setComments(path: String, comments: List<String>) {}

    private fun loadConfigOrDefault(): Configuration {
        return if (Files.exists(configPath)) {
            provider.load(configPath.toFile(), defaultConfig)
        } else {
            Configuration(defaultConfig)
        }
    }
}
