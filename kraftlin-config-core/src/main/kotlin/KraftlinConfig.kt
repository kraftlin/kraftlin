package io.github.kraftlin.config

import java.util.*
import kotlin.enums.enumEntries
import kotlin.reflect.KProperty

/**
 * Base class for any plugin configuration to extend. It provides the [config] methods to create property delegates.
 * Example usage:
 * ```kotlin
 * class Configuration(plugin: Plugin) : AbstractConfig(wrapConfig(plugin)) {
 *     val value1: String by config("path1", default = "default value")
 *     val value2: Int by config("path2.subpath2", default = 11)
 *     val value3: List<Material> by config("path3", default = listOf(Material.DIRT, Material.STONE))
 *     val value4: CustomObject by config(
 *         "path4",
 *         default = CustomObject("object"),
 *         serialize = CustomObject::toString,
 *         deserialize = { CustomObject(it.lowercase()) }
 *     )
 * }
 * ```
 * It also provides methods to save defaults [saveDefaults] and reload config values [reloadConfig]
 */
public abstract class AbstractConfig protected constructor(protected val configWrapper: ConfigWrapper) {

    private val delegates: MutableCollection<ConfigDelegate<*>> = mutableListOf()

    /**
     * Reloads all config values. Invokes the provided [ConfigWrapper.reloadConfig].
     */
    public fun reloadConfig() {
        delegates.forEach { it.reloaded() }
        configWrapper.reloadConfig()
    }

    /**
     * Writes all provided default values to the underlying config store and reloads the updated config values.
     */
    public fun saveDefaults() {
        configWrapper.saveDefaults()
        configWrapper.reloadConfig()
        delegates.forEach { it.applyComments() }
        configWrapper.save()
        configWrapper.reloadConfig()
    }

    /**
     * Writes any changes in configuration values to disk.
     * WARNING: overwrites any changes done to the config file on disk!
     */
    public fun save() {
        configWrapper.save()
    }

    /**
     * Removes any keys from the configuration that are not associated with any registered delegate.
     * This helps keeping the configuration file clean from legacy settings.
     */
    public fun removeRedundantKeys() {
        val activePaths = delegates.map { it.path }.toSet()
        val existingKeys = configWrapper.getKeys(deep = true)

        val redundantKeys = existingKeys.filter { key ->
            activePaths.none { active ->
                active == key || active.startsWith("$key.") || key.startsWith("$active.")
            }
        }

        redundantKeys.forEach { configWrapper.remove(it) }
    }

    /**
     * Config delegate for [Boolean] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    protected fun config(
        path: String,
        default: Boolean,
        vararg comments: String
    ): ConfigDelegate<Boolean> {
        configWrapper.addDefault(path, default)
        return ConfigDelegate(
            path,
            { pathInner -> configWrapper.getBoolean(pathInner) },
            { pathInner, value -> configWrapper.set(pathInner, value) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [Int] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    protected fun config(
        path: String,
        default: Int,
        vararg comments: String
    ): ConfigDelegate<Int> {
        configWrapper.addDefault(path, default)
        return ConfigDelegate(
            path,
            { pathInner -> configWrapper.getInt(pathInner) },
            { pathInner, value -> configWrapper.set(pathInner, value) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [Long] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    protected fun config(
        path: String,
        default: Long,
        vararg comments: String
    ): ConfigDelegate<Long> {
        configWrapper.addDefault(path, default)
        return ConfigDelegate(
            path,
            { pathInner -> configWrapper.getLong(pathInner) },
            { pathInner, value -> configWrapper.set(pathInner, value) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [Double] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    protected fun config(
        path: String,
        default: Double,
        vararg comments: String
    ): ConfigDelegate<Double> {
        configWrapper.addDefault(path, default)
        return ConfigDelegate(
            path,
            { pathInner -> configWrapper.getDouble(pathInner) },
            { pathInner, value -> configWrapper.set(pathInner, value) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [String] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    protected fun config(
        path: String,
        default: String,
        vararg comments: String
    ): ConfigDelegate<String> {
        configWrapper.addDefault(path, default)
        return ConfigDelegate(
            path,
            { pathInner -> configWrapper.getString(pathInner) },
            { pathInner, value -> configWrapper.set(pathInner, value) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [Enum] values. The value is stored by its lower cased [Enum.name] and matched
     * case-insensitively, ignoring surrounding whitespace and treating '-' as '_'. An unknown name is reported via
     * [ConfigWrapper.warn], listing the valid values (or naming the enum type if it has many), and [default] is used
     * instead.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    protected inline fun <reified T : Enum<T>> config(
        path: String,
        default: T,
        vararg comments: String
    ): ConfigDelegate<T> =
        config(path, default, { it.name.lowercase() }, { parseEnum(it, enumEntries<T>()) }, *comments)

    /**
     * Config delegate for [UUID] values. The UUID is stored and retrieved by its string representation [UUID.toString].
     * A value that [UUID.fromString] rejects is reported via [ConfigWrapper.warn] and [default] is used instead.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    protected fun config(
        path: String,
        default: UUID,
        vararg comments: String
    ): ConfigDelegate<UUID> =
        config(path, default, UUID::toString, UUID::fromString, *comments)

    /**
     * Config delegate for arbitrary types. They are stored and retrieved by the provided [serialize] and [deserialize]
     * functions respectively. If [deserialize] throws for the stored value, a warning is reported via
     * [ConfigWrapper.warn] and [default] is used instead.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param serialize A function to serialize the custom type to a string
     * @param deserialize A function to deserialize a custom type from string
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    protected fun <T> config(
        path: String,
        default: T,
        serialize: (T) -> String,
        deserialize: (String) -> T,
        vararg comments: String
    ): ConfigDelegate<T> {
        configWrapper.addDefault(path, serialize(default))
        return ConfigDelegate(
            path,
            { pathInner -> readValue(pathInner, default, deserialize) },
            { pathInner, value -> configWrapper.set(pathInner, serialize(value)) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [List] of [Boolean] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    @JvmName("configListBoolean")
    protected fun config(
        path: String,
        default: List<Boolean>,
        vararg comments: String
    ): ConfigDelegate<List<Boolean>> {
        configWrapper.addDefault(path, default)
        return ConfigDelegate(
            path,
            { pathInner -> configWrapper.getBooleanList(pathInner) },
            { pathInner, value -> configWrapper.set(pathInner, value) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [List] of [Int] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    @JvmName("configListInt")
    protected fun config(
        path: String,
        default: List<Int>,
        vararg comments: String
    ): ConfigDelegate<List<Int>> {
        configWrapper.addDefault(path, default)
        return ConfigDelegate(
            path,
            { pathInner -> configWrapper.getIntegerList(pathInner) },
            { pathInner, value -> configWrapper.set(pathInner, value) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [List] of [Long] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    @JvmName("configListLong")
    protected fun config(
        path: String,
        default: List<Long>,
        vararg comments: String
    ): ConfigDelegate<List<Long>> {
        configWrapper.addDefault(path, default)
        return ConfigDelegate(
            path,
            { pathInner -> configWrapper.getLongList(pathInner) },
            { pathInner, value -> configWrapper.set(pathInner, value) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [List] of [Double] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    @JvmName("configListDouble")
    protected fun config(
        path: String,
        default: List<Double>,
        vararg comments: String
    ): ConfigDelegate<List<Double>> {
        configWrapper.addDefault(path, default)
        return ConfigDelegate(
            path,
            { pathInner -> configWrapper.getDoubleList(pathInner) },
            { pathInner, value -> configWrapper.set(pathInner, value) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [List] of [String] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    @JvmName("configListString")
    protected fun config(
        path: String,
        default: List<String>,
        vararg comments: String
    ): ConfigDelegate<List<String>> {
        configWrapper.addDefault(path, default)
        return ConfigDelegate(
            path,
            { pathInner -> configWrapper.getStringList(pathInner) },
            { pathInner, value -> configWrapper.set(pathInner, value) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [List] of [Enum] values. Each value is stored by its lower cased [Enum.name] and matched
     * case-insensitively, ignoring surrounding whitespace and treating '-' as '_'. Unknown names are skipped with a
     * warning reported via [ConfigWrapper.warn], listing the valid values (or naming the enum type if it has many).
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    @JvmName("configListEnum")
    protected inline fun <reified T : Enum<T>> config(
        path: String,
        default: List<T>,
        vararg comments: String
    ): ConfigDelegate<List<T>> =
        config<T>(path, default, { it.name.lowercase() }, { parseEnum(it, enumEntries<T>()) }, *comments)

    /**
     * Config delegate for [List] of [UUID] values. The UUID is stored and retrieved by its string representation [UUID.toString].
     * Values that [UUID.fromString] rejects are skipped with a warning reported via [ConfigWrapper.warn].
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    @JvmName("configListUuid")
    protected fun config(
        path: String,
        default: List<UUID>,
        vararg comments: String
    ): ConfigDelegate<List<UUID>> =
        this.config(path, default, UUID::toString, UUID::fromString, *comments)

    /**
     * Config delegate for [List] of arbitrary values. They are stored as string lists and retrieved by the provided
     * [serialize] and [deserialize] functions respectively. Elements for which [deserialize] throws are skipped with a
     * warning reported via [ConfigWrapper.warn].
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param serialize A function to serialize the custom type to a string
     * @param deserialize A function to deserialize a custom type from string
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    @JvmName("configListT")
    // No inline because it will cause issues with configWrapper access scope
    protected fun <T> config(
        path: String,
        default: List<T>,
        serialize: (T) -> String,
        deserialize: (String) -> T,
        vararg comments: String
    ): ConfigDelegate<List<T>> {
        configWrapper.addDefault(path, default.map(serialize))
        return ConfigDelegate(
            path,
            { readList(it, deserialize) },
            { pathInner, value -> configWrapper.set(pathInner, value.map(serialize)) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [Map] of [String] keys and values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    @JvmName("configMapString")
    protected fun config(
        path: String,
        default: Map<String, String>,
        vararg comments: String
    ): ConfigDelegate<Map<String, String>> =
        config(path, default, serialize = { s: String -> s }, deserialize = { s: String -> s }, comments = comments)

    /**
     * Config delegate for [Map] of [String] keys with arbitrary values. They are stored as [String] values and
     * retrieved by the provided [serialize] and [deserialize] functions respectively. Entries for which [deserialize]
     * throws are skipped with a warning reported via [ConfigWrapper.warn].
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param serialize A function to serialize the custom type to a string
     * @param deserialize A function to deserialize a custom type from string
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    @JvmName("configMapT")
    protected fun <T> config(
        path: String,
        default: Map<String, T>,
        serialize: (T) -> String,
        deserialize: (String) -> T,
        vararg comments: String
    ): ConfigDelegate<Map<String, T>> {
        configWrapper.addDefault(path, default.mapValues { (_, v) -> serialize(v) })
        return ConfigDelegate(
            path,
            { readMap(it, deserialize) },
            { pathInner, value -> configWrapper.set(pathInner, value.mapValues { (_, v) -> serialize(v) }) },
            comments.toList()
        )
    }

    private fun <T> readValue(path: String, default: T, deserialize: (String) -> T): T {
        val raw = configWrapper.getString(path)
        return try {
            deserialize(raw)
        } catch (e: Exception) {
            configWrapper.warn("Config value at '$path' could not be read ('$raw': ${e.reason}), using default")
            default
        }
    }

    private fun <T> readList(path: String, deserialize: (String) -> T): List<T> {
        val result = mutableListOf<T>()
        for (element in configWrapper.getStringList(path)) {
            try {
                result.add(deserialize(element))
            } catch (e: Exception) {
                configWrapper.warn(
                    "Config list element at '$path' could not be read ('$element': ${e.reason}), skipping"
                )
            }
        }
        return result
    }

    private fun <T> readMap(path: String, deserialize: (String) -> T): Map<String, T> {
        val result = mutableMapOf<String, T>()
        for ((key, value) in configWrapper.getMap(path)) {
            val raw = value.toString()
            try {
                result[key] = deserialize(raw)
            } catch (e: Exception) {
                configWrapper.warn("Config value at '$path.$key' could not be read ('$raw': ${e.reason}), skipping")
            }
        }
        return result
    }

    private val Exception.reason: String
        get() = message?.takeIf { it.isNotBlank() } ?: javaClass.name

    /**
     * A property delegate for storage in a backing [ConfigWrapper]. See [AbstractConfig] for usage.
     */
    public inner class ConfigDelegate<T>(
        internal val path: String,
        private val producer: (String) -> T,
        private val consumer: (String, T) -> Unit,
        private val comments: List<String> = emptyList()
    ) {

        init {
            delegates.add(this)
        }

        private var cache: T? = null
        private var loaded: Boolean = false

        internal fun applyComments() {
            if (comments.isNotEmpty() && configWrapper.getComments(path).isEmpty()) {
                configWrapper.setComments(path, comments)
            }
        }

        public operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!loaded) {
                cache = producer(path)
                loaded = true
            }
            @Suppress("UNCHECKED_CAST")
            return cache as T
        }

        public operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            loaded = false
            cache = null
            consumer(path, value)
        }

        internal fun reloaded() {
            cache = null
            loaded = false
        }
    }

    /**
     * An abstraction for an underlying configuration store.
     * Decouples config delegates from the underlying config framework.
     */
    public interface ConfigWrapper {

        /**
         * Gets all keys present in the config store.
         *
         * @param deep Whether to include keys from nested sections
         */
        public fun getKeys(deep: Boolean): Set<String>

        /**
         * Removes the value at the specified path.
         *
         * @param path The dot separated configuration value path
         */
        public fun remove(path: String)

        /**
         * Adds a default value to be used in absence of a value provided by the client.
         *
         * @param path The dot separated configuration value path
         * @param value The default configuration value
         */
        public fun addDefault(path: String, value: Any)

        /**
         * Gets the specified [Boolean] configuration value. If the value is not set or not a valid boolean,
         * the default value provided by [addDefault] is returned.
         *
         * @param path The dot separated configuration value path
         */
        public fun getBoolean(path: String): Boolean

        /**
         * Gets the specified [Int] configuration value. If the value is not set or not a valid integer,
         * the default value provided by [addDefault] is returned.
         *
         * @param path The dot separated configuration value path
         */
        public fun getInt(path: String): Int

        /**
         * Gets the specified [Long] configuration value. If the value is not set or not a valid long,
         * the default value provided by [addDefault] is returned.
         *
         * @param path The dot separated configuration value path
         */
        public fun getLong(path: String): Long

        /**
         * Gets the specified [Double] configuration value. If the value is not set or not a valid double,
         * the default value provided by [addDefault] is returned.
         *
         * @param path The dot separated configuration value path
         */
        public fun getDouble(path: String): Double

        /**
         * Gets the specified [String] configuration value. If the value is not set or not a valid string,
         * the default value provided by [addDefault] is returned. Values loaded from the file are returned as typed
         * there (`1.10` stays `1.10`, `007` stays `007`).
         *
         * @param path The dot separated configuration value path
         */
        public fun getString(path: String): String

        /**
         * Gets the specified [List] of [Boolean] configuration value. If the value is not set or not a valid boolean
         * list, the default value provided by [addDefault] is returned.
         * A key present without a value reads as an empty list. Elements that cannot be converted are skipped with
         * a warning.
         *
         * @param path The dot separated configuration value path
         */
        public fun getBooleanList(path: String): List<Boolean>

        /**
         * Gets the specified [List] of [Int] configuration value. If the value is not set or not a valid integer
         * list, the default value provided by [addDefault] is returned.
         * A key present without a value reads as an empty list. Elements that cannot be converted are skipped with
         * a warning.
         *
         * @param path The dot separated configuration value path
         */
        public fun getIntegerList(path: String): List<Int>

        /**
         * Gets the specified [List] of [Long] configuration value. If the value is not set or not a valid long
         * list, the default value provided by [addDefault] is returned.
         * A key present without a value reads as an empty list. Elements that cannot be converted are skipped with
         * a warning.
         *
         * @param path The dot separated configuration value path
         */
        public fun getLongList(path: String): List<Long>

        /**
         * Gets the specified [List] of [Double] configuration value. If the value is not set or not a valid double
         * list, the default value provided by [addDefault] is returned.
         * A key present without a value reads as an empty list. Elements that cannot be converted are skipped with
         * a warning.
         *
         * @param path The dot separated configuration value path
         */
        public fun getDoubleList(path: String): List<Double>

        /**
         * Gets the specified [List] of [String] configuration value. If the value is not set or not a valid string
         * list, the default value provided by [addDefault] is returned.
         * A key present without a value reads as an empty list. Elements that cannot be converted are skipped with
         * a warning. Elements loaded from the file are returned as typed there (`1.10` stays `1.10`).
         *
         * @param path The dot separated configuration value path
         */
        public fun getStringList(path: String): List<String>

        /**
         * Gets the specified flat [Map] indexed by [String] configuration value. If the value is not set
         * or not a valid Map, the default value provided by [addDefault] is returned. A key present without a value
         * reads as an empty map. Entries without a value, lists and nested sections are skipped with a warning.
         * Values are returned as [String], whether loaded from the file, set or provided by [addDefault]: values
         * loaded from the file as typed there (`1.10` stays `1.10`), other values converted with [toString].
         *
         * @param path The dot separated configuration value path
         */
        public fun getMap(path: String): Map<String, Any>

        /**
         * Sets the value in the underlying config. Use [save] to persist changes.
         *
         * @param path The dot separated configuration value path
         * @param value The new configuration value
         */
        public fun set(path: String, value: Any?)

        /**
         * Reloads all configuration values from the underlying config representation, for example from file.
         *
         * @throws ConfigException if the configuration file cannot be read or parsed.
         *     The previous working configuration remains active.
         */
        public fun reloadConfig()

        /**
         * Stores the default values provided by [addDefault] for any path that has no user provided value.
         * If the underlying config representation does not exist, it is created and initiated with the default values.
         * A list or map key that is present without a value is written as an empty list or map instead of the default.
         *
         * @throws ConfigException if the configuration file or its parent directories cannot be created or written.
         */
        public fun saveDefaults()

        /**
         * Persists any changes in configuration values to disk.
         * WARNING: overwrites any changes done to the config file on disk!
         *
         * @throws ConfigException if the configuration cannot be written to disk.
         *     The in-memory configuration is not affected.
         */
        public fun save()

        /**
         * Returns the comment lines of the key at [path] without the leading `# `, or an empty list if none.
         * The file header is not part of the first key's comments.
         */
        public fun getComments(path: String): List<String>

        /**
         * Replaces the comment lines of the key at [path]. Entries containing line breaks are split into several
         * lines. Applied on the next [save].
         */
        public fun setComments(path: String, comments: List<String>)

        /**
         * Reports a problem with a configuration value, e.g. a list element that was skipped because it is invalid.
         * Defaults to the `io.github.kraftlin.config` logger; wrappers created by `wrapConfig` use their own logger.
         */
        public fun warn(message: String) {
            defaultLogger.warning(message)
        }
    }
}

private const val MAX_LISTED_ENUM_NAMES = 20

/**
 * Finds the entry whose name matches [value] case-insensitively, ignoring surrounding whitespace and treating '-' as
 * '_'. Used by the inline enum delegates of [AbstractConfig].
 *
 * @throws IllegalArgumentException if no entry matches, listing the valid lower cased names unless there are more than
 * [MAX_LISTED_ENUM_NAMES]
 */
@PublishedApi
internal fun <T : Enum<T>> parseEnum(value: String, entries: List<T>): T {
    val name = value.trim().replace('-', '_')
    return entries.firstOrNull { it.name.replace('-', '_').equals(name, ignoreCase = true) }
        ?: throw IllegalArgumentException(
            if (entries.size <= MAX_LISTED_ENUM_NAMES) "expected one of ${entries.joinToString { it.name.lowercase() }}"
            else "expected a valid ${entries.first().declaringJavaClass.simpleName} name"
        )
}
