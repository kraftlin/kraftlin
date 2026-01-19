package io.github.kraftlin.config

import java.util.*
import kotlin.reflect.KProperty

/**
 * Base class for any plugin configuration to extend. It provides the [config] methods to create property delegates.
 * Example usage:
 * ```kotlin
 *     class Configuration(plugin: Plugin): AbstractConfig(wrapConfig(plugin)) {
 *         val value1: String by config("path1", default = "default value")
 *         val value2: Int by config("path2.subpath2", default = 11)
 *         val value3: List<Material> by config("path3", default = listOf(DIRT, GRASS, STONE)
 *         val value4: CustomObject by config("path4", default = CustomObject("object"),
 *                                            serialize = CustomObject::toString(),
 *                                            deserialize = { CustomObject(it.toLowerCase()) }
 *                                     )
 *     }
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
     * Config delegate for [Boolean] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
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
     */
    protected fun config(
        path: String,
        default: String,
        translateColorCodes: Boolean = false,
        vararg comments: String
    ): ConfigDelegate<String> {
        configWrapper.addDefault(path, default)
        return if (translateColorCodes) {
            ConfigDelegate(
                path,
                { pathInner -> configWrapper.getString(pathInner).replace('&', '§') },
                { pathInner, value -> configWrapper.set(pathInner, value.replace('§', '&')) },
                comments.toList()
            )
        } else {
            ConfigDelegate(
                path,
                { pathInner -> configWrapper.getString(pathInner) },
                { pathInner, value -> configWrapper.set(pathInner, value) },
                comments.toList()
            )
        }
    }

    /**
     * Config delegate for [Enum] values. The value is stored and retrieved by its lower cased [Enum.name].
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     */
    protected inline fun <reified T : Enum<T>> config(
        path: String,
        default: T,
        vararg comments: String
    ): ConfigDelegate<T> =
        config(path, default, { it.name.lowercase() }, { enumValueOf(it.uppercase().replace('-', '_')) }, *comments)

    /**
     * Config delegate for [UUID] values. The UUID is stored and retrieved by its string representation [UUID.toString].
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     */
    protected fun config(
        path: String,
        default: UUID,
        vararg comments: String
    ): ConfigDelegate<UUID> =
        config(path, default, UUID::toString, UUID::fromString, *comments)

    /**
     * Config delegate for arbitrary types. They are stored and retrieved by the provided [serialize] and [deserialize]
     * functions respectively.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param serialize A function to serialize the custom type to a string
     * @param deserialize A function to deserialize a custom type from string
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
            { pathInner -> deserialize(configWrapper.getString(pathInner)) },
            { pathInner, value -> configWrapper.set(pathInner, serialize(value)) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [List] of [Boolean] values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
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
     */
    @JvmName("configListString")
    protected fun config(
        path: String,
        default: List<String>,
        translateColorCodes: Boolean = false,
        vararg comments: String
    ): ConfigDelegate<List<String>> {
        configWrapper.addDefault(path, default)
        return if (translateColorCodes) {
            ConfigDelegate(
                path,
                { pathInner -> configWrapper.getStringList(pathInner).map { it.replace('&', '§') } },
                { pathInner, value -> configWrapper.set(pathInner, value.map { it.replace('§', '&') }) },
                comments.toList()
            )
        } else {
            ConfigDelegate(
                path,
                { pathInner -> configWrapper.getStringList(pathInner) },
                { pathInner, value -> configWrapper.set(pathInner, value) },
                comments.toList()
            )
        }
    }

    /**
     * Config delegate for [List] of [Enum] values. The value is stored and retrieved by its lower cased [Enum.name].
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     */
    @JvmName("configListEnum")
    protected inline fun <reified T : Enum<T>> config(
        path: String,
        default: List<T>,
        vararg comments: String
    ): ConfigDelegate<List<T>> =
        config<T>(path, default, { it.name.lowercase() }, { enumValueOf(it.uppercase().replace('-', '_')) }, *comments)

    /**
     * Config delegate for [List] of [UUID] values. The UUID is stored and retrieved by its string representation [UUID.toString].
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
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
     * [serialize] and [deserialize] functions respectively.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param serialize A function to serialize the custom type to a string
     * @param deserialize A function to deserialize a custom type from string
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
            { configWrapper.getStringList(it).map(deserialize) },
            { pathInner, value -> configWrapper.set(pathInner, value.map(serialize)) },
            comments.toList()
        )
    }

    /**
     * Config delegate for [Map] of [String] keys and values.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
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
     * retrieved by the provided [serialize] and [deserialize] functions respectively.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry
     * @param serialize A function to serialize the custom type to a string
     * @param deserialize A function to deserialize a custom type from string
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
            { configWrapper.getMap(it).mapValues { (_, v) -> deserialize(v.toString()) } },
            { pathInner, value -> configWrapper.set(pathInner, value.mapValues { (_, v) -> serialize(v) }) },
            comments.toList()
        )
    }

    /**
     * A property delegate for storage in a backing [ConfigWrapper]. See [AbstractConfig] for usage.
     */
    public inner class ConfigDelegate<T>(
        private val path: String,
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
         * the default value provided by [addDefault] is returned.
         *
         * @param path The dot separated configuration value path
         */
        public fun getString(path: String): String

        /**
         * Gets the specified [List] of [Boolean] configuration value. If the value is not set or not a valid boolean
         * list, the default value provided by [addDefault] is returned.
         *
         * @param path The dot separated configuration value path
         */
        public fun getBooleanList(path: String): List<Boolean>

        /**
         * Gets the specified [List] of [Int] configuration value. If the value is not set or not a valid integer
         * list, the default value provided by [addDefault] is returned.
         *
         * @param path The dot separated configuration value path
         */
        public fun getIntegerList(path: String): List<Int>

        /**
         * Gets the specified [List] of [Long] configuration value. If the value is not set or not a valid long
         * list, the default value provided by [addDefault] is returned.
         *
         * @param path The dot separated configuration value path
         */
        public fun getLongList(path: String): List<Long>

        /**
         * Gets the specified [List] of [Double] configuration value. If the value is not set or not a valid double
         * list, the default value provided by [addDefault] is returned.
         *
         * @param path The dot separated configuration value path
         */
        public fun getDoubleList(path: String): List<Double>

        /**
         * Gets the specified [List] of [String] configuration value. If the value is not set or not a valid string
         * list, the default value provided by [addDefault] is returned.
         *
         * @param path The dot separated configuration value path
         */
        public fun getStringList(path: String): List<String>

        /**
         * Gets the specified flat [Map] of [Any] indexed by [String] configuration value. If the value is not set
         * or not a valid Map, the default value provided by [addDefault] is returned.
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
         */
        public fun reloadConfig()

        /**
         * Stores the default values provided by [addDefault] for any path that has no user provided value.
         * If the underlying config representation does not exist, it is created and initiated with the default values.
         */
        public fun saveDefaults()

        /**
         * Persists any changes in configuration values to disk.
         * WARNING: overwrites any changes done to the config file on disk!
         */
        public fun save()

        public fun getComments(path: String): List<String>
        public fun setComments(path: String, comments: List<String>)
    }
}
