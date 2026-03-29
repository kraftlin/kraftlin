package io.github.kraftlin.config.bungee

import io.github.kraftlin.config.AbstractConfig
import io.github.kraftlin.config.wrapConfig as coreWrapConfig
import net.md_5.bungee.api.plugin.Plugin
import java.nio.file.Path
import java.util.logging.Logger

/**
 * Wraps the default plugin config `config.yml` for use in [AbstractConfig].
 * This is the recommended approach when using a single configuration file only.
 *
 * @param plugin The plugin instance to which the config belongs.
 * @param logger Logger used for type coercion warnings.
 */
public fun wrapConfig(
    plugin: Plugin,
    logger: Logger = Logger.getLogger("io.github.kraftlin.config")
): AbstractConfig.ConfigWrapper =
    coreWrapConfig(plugin.dataFolder.toPath().resolve("config.yml"), logger)

/**
 * Wraps an arbitrary `YAML` file for use in [AbstractConfig]. This way, a plugin can support multiple configurations.
 */
@Deprecated(
    "Use io.github.kraftlin.config.wrapConfig(Path) instead.",
    ReplaceWith("io.github.kraftlin.config.wrapConfig(configPath)", "io.github.kraftlin.config.wrapConfig")
)
public fun wrapConfig(configPath: Path): AbstractConfig.ConfigWrapper =
    coreWrapConfig(configPath)
