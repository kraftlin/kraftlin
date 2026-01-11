package io.github.kraftlin.config

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.Properties

/**
 * Access configuration for a database. Used to load devsylum conforming database configurations.
 * See https://gitlab.devsylum.de/asylum/DevWiki/wikis/database-configuration for definitions.
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
 * Loads a [io.github.kraftlin.config.SqlConfiguration] from file, optionally storing a default example configuration if none exists.
 *
 * The configuration file name is `database.properties` in the directory [dataFolder]. The file is stored and read in
 * `UTF-8` format.
 *
 * If [saveDefault] is `true`, this method creates a default configuration with example properties if none exists. It
 * also creates any parent directories required to store it.
 *
 * @param dataFolder The plugins data folder root
 * @param saveDefault Stores an example configuration if the file does not exist
 */
public fun loadSqlConfiguration(dataFolder: Path, saveDefault: Boolean = true): SqlConfiguration {
    val configFile = dataFolder.resolve("database.properties")

    if (saveDefault && !Files.exists(configFile)) {
        val defaultConfig = Properties().apply {
            setProperty("url", "jdbc:mysql://localhost:3306/dbname")
            setProperty("user", "exampleuser")
            setProperty("password", "examplepassword")
        }

        Files.createDirectories(dataFolder)
        Files.newBufferedWriter(configFile, Charsets.UTF_8, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
            .use { writer ->
                defaultConfig.store(
                    writer,
                    "Database configuration as defined in https://gitlab.devsylum.de/asylum/DevWiki/wikis/database-configuration"
                )
            }
    }

    val databaseConfig = Properties()
    Files.newBufferedReader(configFile, Charsets.UTF_8).use(databaseConfig::load)

    return SqlConfiguration(
        url = databaseConfig.getProperty("url"),
        user = databaseConfig.getProperty("user"),
        password = databaseConfig.getProperty("password")
    )
}
