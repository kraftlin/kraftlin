package io.github.kraftlin.config

import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

/**
 * Access configuration for a database.
 *
 * @property url The JDBC database url
 * @property user The username used to access the database
 * @property password The database password for [user]
 */
public data class SqlConfiguration(val url: String, val user: String, val password: String) {

    override fun toString(): String {
        return "SqlConfiguration(url='$url', user='$user', password='********')"
    }
}

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

    val load = Load(yamlLoadSettings)
    val config: Map<*, *>
    try {
        val parsed = load.loadFromString(Files.readString(configFile))
        config = parsed as? Map<*, *>
            ?: throw IllegalStateException("Database configuration at $configFile is not a YAML mapping")
    } catch (exception: IOException) {
        throw IllegalStateException("Failed to read database configuration at $configFile", exception)
    } catch (exception: YamlEngineException) {
        throw IllegalStateException("Invalid YAML in database configuration at $configFile", exception)
    }

    return SqlConfiguration(
        url = requireConfigValue(config, "url", configFile),
        user = requireConfigValue(config, "user", configFile),
        password = requireConfigValue(config, "password", configFile)
    )
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
    val yaml = linkedMapOf<String, String>(
        "url" to config.url,
        "user" to config.user,
        "password" to config.password
    )
    val dump = org.snakeyaml.engine.v2.api.Dump(yamlDumpSettings)
    Files.writeString(configFile, dump.dumpToString(yaml))
}

private fun requireConfigValue(config: Map<*, *>, key: String, configFile: Path): String {
    val value = config[key]?.toString()?.trim()
    if (value.isNullOrEmpty()) {
        throw IllegalStateException("Missing required database configuration key '$key' in $configFile")
    }
    return value
}
