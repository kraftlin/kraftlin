package io.github.kraftlin.config.paper

import io.github.kraftlin.config.SqlConfiguration
import io.github.kraftlin.config.loadSqlConfiguration as coreLoadSqlConfiguration
import org.bukkit.plugin.Plugin
import java.nio.file.Path

/**
 * Loads a [SqlConfiguration] from `database.yml` in the plugin data folder.
 *
 * @param plugin The plugin instance to which the config belongs
 * @param saveDefault Stores an example configuration if the file does not exist
 */
public fun loadSqlConfiguration(plugin: Plugin, saveDefault: Boolean = true): SqlConfiguration =
    coreLoadSqlConfiguration(plugin.dataPath, saveDefault)

/**
 * Loads a [SqlConfiguration] from `database.yml` in [dataFolder].
 *
 * @param dataFolder The plugins data folder root
 * @param saveDefault Stores an example configuration if the file does not exist
 */
@Deprecated(
    "Use io.github.kraftlin.config.loadSqlConfiguration(Path, Boolean) instead.",
    ReplaceWith(
        "io.github.kraftlin.config.loadSqlConfiguration(dataFolder, saveDefault)",
        "io.github.kraftlin.config.loadSqlConfiguration"
    )
)
public fun loadSqlConfiguration(dataFolder: Path, saveDefault: Boolean = true): SqlConfiguration =
    coreLoadSqlConfiguration(dataFolder, saveDefault)
