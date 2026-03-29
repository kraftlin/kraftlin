package io.github.kraftlin.config.bungee

import io.github.kraftlin.config.SqlConfiguration
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.config.Configuration
import net.md_5.bungee.config.ConfigurationProvider
import net.md_5.bungee.config.YamlConfiguration
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/**
 * Loads a [SqlConfiguration] from file, optionally storing a default example configuration if none exists.
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

    val provider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)
    val config: Configuration
    try {
        config = provider.load(configFile.toFile())
    } catch (exception: IOException) {
        throw IllegalStateException("Failed to read database configuration at $configFile", exception)
    }

    return SqlConfiguration(
        url = requireConfigValue(config, "url", configFile),
        user = requireConfigValue(config, "user", configFile),
        password = requireConfigValue(config, "password", configFile)
    )
}

/**
 * Loads a [SqlConfiguration] from `database.yml` in the plugin data folder.
 *
 * @param plugin The plugin instance to which the config belongs
 * @param saveDefault Stores an example configuration if the file does not exist
 */
public fun loadSqlConfiguration(plugin: Plugin, saveDefault: Boolean = true): SqlConfiguration {
    return loadSqlConfiguration(plugin.dataFolder.toPath(), saveDefault)
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
    val provider = ConfigurationProvider.getProvider(YamlConfiguration::class.java)
    val yaml = Configuration()
    yaml.set("url", config.url)
    yaml.set("user", config.user)
    yaml.set("password", config.password)
    provider.save(yaml, configFile.toFile())
}

private fun requireConfigValue(config: Configuration, key: String, configFile: Path): String {
    val value = config.getString(key)?.trim()
    if (value.isNullOrEmpty()) {
        throw IllegalStateException("Missing required database configuration key '$key' in $configFile")
    }
    return value
}
