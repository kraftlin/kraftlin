package io.github.kraftlin.config

import java.nio.file.Path

/**
 * Thrown when a configuration file cannot be loaded, parsed, or saved.
 *
 * The [message] is written for server administrators and always includes
 * the file [path] so they know which file to inspect. The original
 * [cause] (if any) carries the technical details for developers.
 *
 * @property path The configuration file that triggered this error
 */
public class ConfigException(
    public val path: Path,
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
