package io.github.kraftlin.config.velocity

import io.github.kraftlin.config.SqlConfiguration
import io.github.kraftlin.config.loadSqlConfiguration as coreLoadSqlConfiguration
import java.nio.file.Path

/**
 * Loads a [SqlConfiguration] from `database.yml` in the plugin data directory.
 *
 * Pass the `@DataDirectory Path` injected into your plugin constructor.
 *
 * @param dataDirectory The plugin's data directory (injected via `@DataDirectory`).
 * @param saveDefault Stores an example configuration if the file does not exist
 */
public fun loadSqlConfiguration(dataDirectory: Path, saveDefault: Boolean = true): SqlConfiguration =
    coreLoadSqlConfiguration(dataDirectory, saveDefault)
