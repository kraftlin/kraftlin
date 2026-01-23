package io.github.kraftlin.config.paper

import io.github.kraftlin.config.SqlConfiguration
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/**
 * Loads a [io.github.kraftlin.config.SqlConfiguration] from file, optionally storing a default example configuration if
 * none exists.
 *
 * The configuration file name is `database.yml` in the directory [dataFolder]. The file is stored and read in
 * `UTF-8` format. Legacy `database.properties` files are migrated to `database.yml` when possible.
 *
 * If [saveDefault] is `true`, this method creates a default configuration with example properties if none exists. It
 * also creates any parent directories required to store it.
 *
 * @param dataFolder The plugins data folder root
 * @param saveDefault Stores an example configuration if the file does not exist
 */
public fun loadSqlConfiguration(dataFolder: Path, saveDefault: Boolean = true): SqlConfiguration {
    val configFile = dataFolder.resolve("database.yml")
    val legacyConfigFile = dataFolder.resolve("database.properties")

    if (!Files.exists(configFile) && Files.exists(legacyConfigFile)) {
        val legacyConfig = loadPropertiesConfiguration(legacyConfigFile)
        writeYamlConfiguration(dataFolder, configFile, legacyConfig)
    }

    if (saveDefault && !Files.exists(configFile)) {
        writeYamlConfiguration(
            dataFolder,
            configFile,
            SqlConfiguration(
                url = "jdbc:mysql://localhost:3306/dbname",
                user = "exampleuser",
                password = "examplepassword"
            )
        )
    }

    val config = YamlConfiguration()
    try {
        config.load(configFile.toFile())
    } catch (exception: IOException) {
        throw IllegalStateException("Failed to read database configuration at $configFile", exception)
    } catch (exception: InvalidConfigurationException) {
        throw IllegalStateException("Invalid YAML in database configuration at $configFile", exception)
    }

    return SqlConfiguration(
        url = requireConfigValue(config, "url", configFile),
        user = requireConfigValue(config, "user", configFile),
        password = requireConfigValue(config, "password", configFile)
    )
}

/**
 * Loads a [io.github.kraftlin.config.SqlConfiguration] from `database.yml` in the plugin data folder.
 *
 * @param plugin The plugin instance to which the config belongs
 * @param saveDefault Stores an example configuration if the file does not exist
 */
public fun loadSqlConfiguration(plugin: Plugin, saveDefault: Boolean = true): SqlConfiguration {
    return loadSqlConfiguration(plugin.dataPath, saveDefault)
}

private fun loadPropertiesConfiguration(configFile: Path): SqlConfiguration {
    val databaseConfig = Properties()
    Files.newBufferedReader(configFile, Charsets.UTF_8).use(databaseConfig::load)
    return SqlConfiguration(
        url = databaseConfig.getProperty("url"),
        user = databaseConfig.getProperty("user"),
        password = databaseConfig.getProperty("password")
    )
}

private fun writeYamlConfiguration(dataFolder: Path, configFile: Path, config: SqlConfiguration) {
    Files.createDirectories(dataFolder)
    val yaml = YamlConfiguration()
    yaml.options().setHeader(
        listOf(
            "Kraftlin database configuration (YAML).",
            "Keys: url, user, password. Example URL: jdbc:mysql://host:3306/dbname",
            "Keep credentials private; do not share this file."
        )
    )
    yaml.addDefault("url", config.url)
    yaml.addDefault("user", config.user)
    yaml.addDefault("password", config.password)
    yaml.options().copyDefaults(true)
    yaml.save(configFile.toFile())
}

private fun requireConfigValue(config: YamlConfiguration, key: String, configFile: Path): String {
    val value = config.getString(key)?.trim()
    if (value.isNullOrEmpty()) {
        throw IllegalStateException("Missing required database configuration key '$key' in $configFile")
    }
    return value
}
