package io.github.kraftlin.config

import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.lowlevel.Compose
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.Tag
import java.io.IOException
import java.io.StringReader
import java.nio.charset.Charset
import java.nio.charset.MalformedInputException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties

private const val DATABASE_CONFIG_HEADER =
    "# Kraftlin database configuration (YAML).\n" +
        "# Keys: url, user, password. Example URL: jdbc:mysql://host:3306/dbname\n" +
        "# Keep credentials private; do not share this file.\n"

/**
 * Access configuration for a database.
 *
 * @property url The JDBC database url
 * @property user The username used to access the database, may be empty (e.g. for SQLite)
 * @property password The database password for [user], may be empty
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
 * `UTF-8` format. A legacy `database.properties` file is migrated to `database.yml` if the latter does not exist yet,
 * and deleted after a successful migration. Migrated values are trimmed.
 *
 * The keys `url`, `user`, and `password` are required. Their values are read as text, without type conversion, so a
 * password like `007007` stays `007007`. `user` and `password` are not trimmed; `url` is. The usual YAML rules still
 * apply: quoted values are unescaped (`'it''s'` becomes `it's`), and a block value (`|` or `>`) keeps its final line
 * break unless written as `|-` or `>-`. `user` and `password` may be empty or YAML null (`~`, `null`), which reads as
 * empty. `url` may not be empty or YAML null.
 *
 * If [saveDefault] is `true`, this method creates a default configuration with example properties if none exists. It
 * also creates any parent directories required to store it.
 *
 * @param dataFolder The plugins data folder root
 * @param saveDefault Stores an example configuration if the file does not exist
 * @throws ConfigException if the configuration (or the legacy file to migrate) cannot be read, written, or is invalid
 */
public fun loadSqlConfiguration(dataFolder: Path, saveDefault: Boolean = true): SqlConfiguration {
    val configFile = dataFolder.resolve("database.yml")
    val legacyConfigFile = dataFolder.resolve("database.properties")

    if (!Files.exists(configFile) && Files.exists(legacyConfigFile)) {
        val legacyConfig = loadPropertiesConfiguration(legacyConfigFile)
        writeYamlConfiguration(dataFolder, configFile, legacyConfig)
        deleteLegacyConfiguration(legacyConfigFile, configFile)
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

    val config: MappingNode
    try {
        val parsed = Compose(yamlLoadSettings).composeString(Files.readString(configFile)).orElse(null)
        config = parsed as? MappingNode
            ?: throw ConfigException(
                configFile,
                "Database configuration at '$configFile' is not a valid YAML mapping.\n" +
                    "The file should contain key-value pairs for 'url', 'user', and 'password'."
            )
    } catch (exception: IOException) {
        throw ConfigException(
            configFile,
            "Could not read database configuration file at '$configFile'.\n" +
                "Please check that the file exists and is readable.",
            exception
        )
    } catch (exception: YamlEngineException) {
        throw ConfigException(
            configFile,
            "Could not read database configuration at '$configFile' because it contains invalid YAML.\n" +
                "Please check the file for syntax errors.",
            exception
        )
    }

    val entries = readEntries(config, configFile)
    val url = requireScalar(entries, "url", configFile)
    if (url.tag == Tag.NULL || url.value.isBlank()) {
        throw ConfigException(
            configFile,
            "Required key 'url' is empty in database configuration at '$configFile'.\n" +
                "Please set it to the JDBC url of your database, e.g. 'jdbc:mysql://host:3306/dbname'."
        )
    }
    return SqlConfiguration(
        url = url.value.trim(),
        user = requireScalar(entries, "user", configFile).textOrEmpty(),
        password = requireScalar(entries, "password", configFile).textOrEmpty()
    )
}

/** The value as typed, or an empty string for YAML null (`~`, `null`), which means the same as an empty value. */
private fun ScalarNode.textOrEmpty(): String = if (tag == Tag.NULL) "" else value

private fun loadPropertiesConfiguration(configFile: Path): SqlConfiguration {
    val databaseConfig = try {
        readLegacyProperties(configFile)
    } catch (e: IOException) {
        throw ConfigException(
            configFile,
            "Could not read legacy database configuration at '$configFile'.\n" +
                "Please check that the file is readable, or delete it and configure 'database.yml' instead.",
            e
        )
    } catch (e: IllegalArgumentException) {
        throw ConfigException(
            configFile,
            "Could not read legacy database configuration at '$configFile' because it contains a malformed " +
                "\\uXXXX escape sequence.\nPlease fix the escape sequence, or delete the file and configure " +
                "'database.yml' instead.",
            e
        )
    }

    // Values are trimmed because earlier versions trimmed them after migration, so credentials that worked keep working.
    val url = databaseConfig.getProperty("url")?.trim()
    if (url.isNullOrEmpty()) {
        throw ConfigException(
            configFile,
            "Could not migrate legacy database configuration at '$configFile' because it has no 'url' entry.\n" +
                "Please add the JDBC url to the file, or delete it and configure 'database.yml' instead."
        )
    }
    return SqlConfiguration(
        url = url,
        user = databaseConfig.getProperty("user", "").trim(),
        password = databaseConfig.getProperty("password", "").trim()
    )
}

/** Reads [file] as `UTF-8`, falling back to `ISO-8859-1` (the [Properties] default) if it is not valid `UTF-8`. */
private fun readLegacyProperties(file: Path): Properties =
    try {
        readProperties(file, Charsets.UTF_8)
    } catch (_: MalformedInputException) {
        readProperties(file, Charsets.ISO_8859_1)
    }

/** Reads [file] with [charset], skipping a byte order mark that [Properties] would read as part of the first key. */
private fun readProperties(file: Path, charset: Charset): Properties =
    Properties().apply { load(StringReader(Files.readString(file, charset).removePrefix("\uFEFF"))) }

private fun deleteLegacyConfiguration(legacyConfigFile: Path, configFile: Path) {
    try {
        Files.delete(legacyConfigFile)
    } catch (e: IOException) {
        throw ConfigException(
            legacyConfigFile,
            "Migrated database configuration to '$configFile', but could not delete '$legacyConfigFile'.\n" +
                "The file is no longer used and still contains the database credentials. Please delete it manually.",
            e
        )
    }
}

private fun writeYamlConfiguration(dataFolder: Path, configFile: Path, config: SqlConfiguration) {
    try {
        Files.createDirectories(dataFolder)
        val yaml = linkedMapOf<String, String>(
            "url" to config.url,
            "user" to config.user,
            "password" to config.password
        )
        val dump = Dump(yamlSaveSettings, YamlRepresenter())
        Files.writeString(configFile, DATABASE_CONFIG_HEADER + dump.dumpToString(yaml))
    } catch (e: IOException) {
        throw ConfigException(
            configFile,
            "Could not write database configuration to '$configFile'.\n" +
                "Please check file system permissions and available disk space.",
            e
        )
    }
}

private fun readEntries(config: MappingNode, configFile: Path): Map<String, Node> {
    val entries = linkedMapOf<String, Node>()
    for (tuple in config.value) {
        val key = tuple.key
        if (entries.put(key, tuple.valueNode) != null) {
            throw ConfigException(
                configFile,
                "Duplicate key '$key' in database configuration at '$configFile'.\n" +
                    "Please remove one of the entries."
            )
        }
    }
    return entries
}

private fun requireScalar(entries: Map<String, Node>, key: String, configFile: Path): ScalarNode {
    val node = entries[key]
        ?: throw ConfigException(
            configFile,
            "Missing required key '$key' in database configuration at '$configFile'.\n" +
                "The file must contain 'url', 'user', and 'password' entries."
        )
    return node as? ScalarNode
        ?: throw ConfigException(
            configFile,
            "Key '$key' in database configuration at '$configFile' is a list or section, but must be a single value.\n" +
                "Please write it as a single (quoted) value, e.g. $key: 'value'."
        )
}
