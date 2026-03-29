package io.github.kraftlin.config.velocity

import io.github.kraftlin.config.AbstractConfig
import io.github.kraftlin.config.wrapConfig as coreWrapConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord

/**
 * Wraps the default plugin config `config.yml` for use in [AbstractConfig].
 * This is the recommended approach when using a single configuration file only.
 *
 * Pass the `@DataDirectory Path` injected into your plugin constructor.
 *
 * @param dataDirectory The plugin's data directory (injected via `@DataDirectory`).
 * @param logger SLF4J logger used for type coercion warnings.
 */
public fun wrapPluginConfig(
    dataDirectory: Path,
    logger: Logger = LoggerFactory.getLogger("io.github.kraftlin.config")
): AbstractConfig.ConfigWrapper =
    coreWrapConfig(dataDirectory.resolve("config.yml"), logger.toJul())

private fun Logger.toJul(): java.util.logging.Logger {
    val slf4j = this
    val jul = java.util.logging.Logger.getAnonymousLogger()
    jul.useParentHandlers = false
    jul.level = Level.ALL
    jul.addHandler(object : Handler() {
        override fun publish(record: LogRecord) {
            val msg = record.message ?: return
            when {
                record.level.intValue() >= Level.SEVERE.intValue() -> slf4j.error(msg, record.thrown)
                record.level.intValue() >= Level.WARNING.intValue() -> slf4j.warn(msg, record.thrown)
                record.level.intValue() >= Level.INFO.intValue() -> slf4j.info(msg, record.thrown)
                record.level.intValue() >= Level.FINE.intValue() -> slf4j.debug(msg, record.thrown)
                else -> slf4j.trace(msg, record.thrown)
            }
        }

        override fun flush() {}
        override fun close() {}
    })
    return jul
}
