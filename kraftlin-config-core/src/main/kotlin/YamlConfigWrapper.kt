package io.github.kraftlin.config

import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.api.lowlevel.Compose
import org.snakeyaml.engine.v2.comments.CommentLine
import org.snakeyaml.engine.v2.comments.CommentType
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.SequenceNode
import org.snakeyaml.engine.v2.representer.StandardRepresenter
import org.snakeyaml.engine.v2.schema.CoreSchema
import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

private val defaultLogger: Logger = Logger.getLogger("io.github.kraftlin.config")

internal val yamlLoadSettings: LoadSettings = LoadSettings.builder()
    .setParseComments(true)
    .setSchema(CoreSchema())
    .build()

internal val yamlDumpSettings: DumpSettings = DumpSettings.builder()
    .setDefaultFlowStyle(FlowStyle.BLOCK)
    .setDumpComments(true)
    .setSchema(CoreSchema())
    .build()

/**
 * Wraps an arbitrary YAML file for use in [AbstractConfig].
 *
 * Uses [snakeyaml-engine](https://github.com/snakeyaml/snakeyaml-engine) (YAML 1.2) internally.
 * This means only `true`/`false` are booleans — `yes`/`no`/`on`/`off` are treated as strings.
 * Comments are fully supported: they are preserved during round-trips and can be set programmatically.
 *
 * Type coercion is applied when reading values: lossless conversions (e.g. `Int` to `Long`, any scalar to `String`)
 * succeed silently. Lossy or impossible conversions log a warning and return the registered default.
 *
 * This implementation is not thread-safe. Callers must ensure that reads, writes, reloads, and saves
 * are not performed concurrently from multiple threads.
 */
public fun wrapConfig(configPath: Path): AbstractConfig.ConfigWrapper =
    wrapConfig(configPath, defaultLogger)

/**
 * Wraps an arbitrary YAML file for use in [AbstractConfig].
 *
 * @param configPath Path to the YAML configuration file
 * @param logger Logger used for type coercion warnings
 * @see wrapConfig
 */
public fun wrapConfig(configPath: Path, logger: Logger): AbstractConfig.ConfigWrapper =
    YamlConfigWrapper(configPath, logger)

private class YamlConfigWrapper(
    private val configPath: Path,
    private val logger: Logger
) : AbstractConfig.ConfigWrapper {

    private var data: MutableMap<String, Any?> = linkedMapOf()
    private val defaults: MutableMap<String, Any?> = linkedMapOf()
    private val comments: MutableMap<String, List<String>> = linkedMapOf()

    init {
        try {
            val result = parseFile()
            if (result != null) {
                data = result.first
                comments.putAll(result.second)
            }
        } catch (_: NoSuchFileException) {
            // File doesn't exist yet — start with empty data, defaults will be applied later
        }
    }

    /**
     * Parses the YAML file without mutating any instance state.
     *
     * @return parsed (data, comments) or `null` if the file is blank or not a mapping
     * @throws ConfigException if the file contains invalid YAML
     * @throws NoSuchFileException if the file does not exist
     */
    private fun parseFile(): Pair<MutableMap<String, Any?>, MutableMap<String, List<String>>>? {
        val yaml = Files.readString(configPath)
        if (yaml.isBlank()) return null

        val compose = Compose(yamlLoadSettings)
        val node = try {
            compose.composeString(yaml).orElse(null) ?: return null
        } catch (e: YamlEngineException) {
            throw ConfigException(
                configPath,
                "Could not read configuration file '$configPath' because it contains invalid YAML.\n" +
                    "Please check the file for syntax errors (mismatched quotes, wrong indentation, stray characters).",
                e
            )
        }

        if (node !is MappingNode) {
            logger.warning("YAML configuration at '$configPath' is not a mapping (found ${node.nodeType}), ignoring content")
            return null
        }

        val parsedData = nodeToMap(node)
        val parsedComments = linkedMapOf<String, List<String>>()
        extractComments(node, null, parsedComments)
        return parsedData to parsedComments
    }

    private fun nodeToMap(node: MappingNode): MutableMap<String, Any?> {
        val map = linkedMapOf<String, Any?>()
        for (tuple in node.value) {
            val keyNode = tuple.keyNode
            if (keyNode is ScalarNode) {
                map[keyNode.value] = nodeToValue(tuple.valueNode)
            }
        }
        return map
    }

    private fun nodeToValue(node: Node): Any? {
        return when (node) {
            is ScalarNode -> resolveScalar(node)
            is MappingNode -> nodeToMap(node)
            is SequenceNode -> node.value.map { nodeToValue(it) }
            else -> null
        }
    }

    private fun resolveScalar(node: ScalarNode): Any? {
        val value = node.value
        val tag = node.tag.value
        return when (tag) {
            "tag:yaml.org,2002:null" -> null
            "tag:yaml.org,2002:bool" -> value.lowercase().toBooleanStrictOrNull()
            "tag:yaml.org,2002:int" -> value.toLongOrNull()?.let { if (it in Int.MIN_VALUE..Int.MAX_VALUE) it.toInt() else it } ?: value
            "tag:yaml.org,2002:float" -> value.toDoubleOrNull() ?: value
            else -> value
        }
    }

    private fun extractComments(node: Node, prefix: String?, target: MutableMap<String, List<String>>) {
        if (node is MappingNode) {
            for (tuple in node.value) {
                val keyNode = tuple.keyNode
                if (keyNode is ScalarNode) {
                    val path = if (prefix != null) "$prefix.${keyNode.value}" else keyNode.value
                    val blockComments = keyNode.blockComments
                    if (blockComments.isNotEmpty()) {
                        target[path] = blockComments
                            .filter { it.commentType == CommentType.BLOCK }
                            .map { it.value.trimStart() }
                    }
                    extractComments(tuple.valueNode, path, target)
                }
            }
        }
    }

    private fun resolve(path: String): Any? {
        val parts = path.split('.')
        var current: Any? = data
        for (part in parts) {
            current = (current as? Map<*, *>)?.get(part) ?: return null
        }
        return current
    }

    private fun resolveOrDefault(path: String): Any? = resolve(path) ?: defaults[path]

    private fun setAtPath(path: String, value: Any?) {
        val parts = path.split('.')
        var current: MutableMap<String, Any?> = data
        for (i in 0 until parts.size - 1) {
            val existing = current[parts[i]]
            @Suppress("UNCHECKED_CAST")
            current = when (existing) {
                is MutableMap<*, *> -> existing as MutableMap<String, Any?>
                else -> linkedMapOf<String, Any?>().also { current[parts[i]] = it }
            }
        }
        if (value == null) {
            current.remove(parts.last())
        } else {
            current[parts.last()] = value
        }
    }

    private fun coerceToBoolean(value: Any?, path: String): Boolean {
        return when (value) {
            is Boolean -> value
            is String -> value.lowercase().toBooleanStrictOrNull() ?: return warnAndDefault(path, value, "Boolean", defaults[path] as? Boolean ?: false)
            null -> (defaults[path] as? Boolean) ?: false
            else -> warnAndDefault(path, value, "Boolean", defaults[path] as? Boolean ?: false)
        }
    }

    private fun coerceToInt(value: Any?, path: String): Int {
        return when (value) {
            is Int -> value
            is Long -> if (value in Int.MIN_VALUE..Int.MAX_VALUE) value.toInt()
            else warnAndDefault(path, value, "Int", defaults[path] as? Int ?: 0)
            is Double -> if (value % 1.0 == 0.0 && value >= Int.MIN_VALUE && value <= Int.MAX_VALUE) value.toInt()
            else warnAndDefault(path, value, "Int", defaults[path] as? Int ?: 0)
            is String -> value.toIntOrNull() ?: warnAndDefault(path, value, "Int", defaults[path] as? Int ?: 0)
            null -> (defaults[path] as? Int) ?: 0
            else -> warnAndDefault(path, value, "Int", defaults[path] as? Int ?: 0)
        }
    }

    private fun coerceToLong(value: Any?, path: String): Long {
        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> if (value % 1.0 == 0.0) value.toLong()
            else warnAndDefault(path, value, "Long", defaults[path] as? Long ?: 0L)
            is String -> value.toLongOrNull() ?: warnAndDefault(path, value, "Long", defaults[path] as? Long ?: 0L)
            null -> (defaults[path] as? Long) ?: 0L
            else -> warnAndDefault(path, value, "Long", defaults[path] as? Long ?: 0L)
        }
    }

    private fun coerceToDouble(value: Any?, path: String): Double {
        return when (value) {
            is Double -> value
            is Int -> value.toDouble()
            is Long -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: warnAndDefault(path, value, "Double", defaults[path] as? Double ?: 0.0)
            null -> (defaults[path] as? Double) ?: 0.0
            else -> warnAndDefault(path, value, "Double", defaults[path] as? Double ?: 0.0)
        }
    }

    private fun coerceToString(value: Any?, path: String): String {
        return when (value) {
            is String -> value
            null -> (defaults[path] as? String) ?: ""
            else -> value.toString()
        }
    }

    private fun <T> coerceToList(value: Any?, path: String, coerceElement: (Any?) -> T?): List<T> {
        if (value == null) {
            @Suppress("UNCHECKED_CAST")
            return (defaults[path] as? List<T>) ?: emptyList()
        }
        if (value !is List<*>) {
            logger.warning("Config value at '$path' is not a List (found ${value::class.simpleName}), using default")
            @Suppress("UNCHECKED_CAST")
            return (defaults[path] as? List<T>) ?: emptyList()
        }
        return value.mapNotNull { element ->
            coerceElement(element) ?: run {
                logger.warning("Config list element at '$path' cannot be converted (found ${element?.let { it::class.simpleName } ?: "null"}), skipping")
                null
            }
        }
    }

    private fun <T> warnAndDefault(path: String, value: Any, targetType: String, default: T): T {
        logger.warning("Config value at '$path' is not a valid $targetType (found ${value::class.simpleName} '$value'), using default value $default")
        return default
    }

    override fun getKeys(deep: Boolean): Set<String> {
        return if (deep) collectKeysDeep(data) else data.keys.toSet()
    }

    private fun collectKeysDeep(map: Map<String, Any?>, prefix: String? = null): Set<String> {
        val keys = mutableSetOf<String>()
        for ((key, value) in map) {
            val fullPath = if (prefix != null) "$prefix.$key" else key
            if (value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                keys.addAll(collectKeysDeep(value as Map<String, Any?>, fullPath))
            } else {
                keys.add(fullPath)
            }
        }
        return keys
    }

    override fun remove(path: String) {
        setAtPath(path, null)
        var current = path
        while ('.' in current) {
            current = current.substringBeforeLast('.')
            val parent = resolve(current)
            if (parent is Map<*, *> && parent.isEmpty()) {
                setAtPath(current, null)
            } else {
                break
            }
        }
    }

    override fun addDefault(path: String, value: Any) {
        defaults[path] = value
    }

    override fun set(path: String, value: Any?) {
        setAtPath(path, value)
    }

    override fun getBoolean(path: String): Boolean = coerceToBoolean(resolveOrDefault(path), path)

    override fun getInt(path: String): Int = coerceToInt(resolveOrDefault(path), path)

    override fun getLong(path: String): Long = coerceToLong(resolveOrDefault(path), path)

    override fun getDouble(path: String): Double = coerceToDouble(resolveOrDefault(path), path)

    override fun getString(path: String): String = coerceToString(resolveOrDefault(path), path)

    override fun getBooleanList(path: String): List<Boolean> =
        coerceToList(resolveOrDefault(path), path) { it as? Boolean ?: (it as? String)?.lowercase()?.toBooleanStrictOrNull() }

    override fun getIntegerList(path: String): List<Int> =
        coerceToList(resolveOrDefault(path), path) {
            when (it) {
                is Int -> it
                is Long -> if (it in Int.MIN_VALUE..Int.MAX_VALUE) it.toInt() else null
                is Double -> if (it % 1.0 == 0.0 && it >= Int.MIN_VALUE && it <= Int.MAX_VALUE) it.toInt() else null
                is String -> it.toIntOrNull()
                else -> null
            }
        }

    override fun getLongList(path: String): List<Long> =
        coerceToList(resolveOrDefault(path), path) {
            when (it) {
                is Long -> it
                is Int -> it.toLong()
                is Double -> if (it % 1.0 == 0.0) it.toLong() else null
                is String -> it.toLongOrNull()
                else -> null
            }
        }

    override fun getDoubleList(path: String): List<Double> =
        coerceToList(resolveOrDefault(path), path) {
            when (it) {
                is Double -> it
                is Int -> it.toDouble()
                is Long -> it.toDouble()
                is String -> it.toDoubleOrNull()
                else -> null
            }
        }

    override fun getStringList(path: String): List<String> =
        coerceToList(resolveOrDefault(path), path) { it?.toString() }

    override fun getMap(path: String): Map<String, Any> {
        val value = resolveOrDefault(path)
        if (value == null) {
            @Suppress("UNCHECKED_CAST")
            return (defaults[path] as? Map<String, Any>) ?: emptyMap()
        }
        if (value !is Map<*, *>) {
            logger.warning("Config value at '$path' is not a Map (found ${value::class.simpleName}), using default")
            @Suppress("UNCHECKED_CAST")
            return (defaults[path] as? Map<String, Any>) ?: emptyMap()
        }
        @Suppress("UNCHECKED_CAST")
        val map = value as Map<String, Any?>
        return map.entries
            .filter { (key, v) ->
                if (v is Map<*, *>) {
                    logger.log(Level.FINE, "Ignoring nested section '$path.$key' — getMap returns flat values only")
                    false
                } else {
                    true
                }
            }
            .associate { (k, v) -> k to (v as Any) }
    }

    override fun reloadConfig() {
        try {
            val result = parseFile()
            // Parse succeeded — safe to swap
            if (result != null) {
                data = result.first
                comments.clear()
                comments.putAll(result.second)
            } else {
                data = linkedMapOf()
                comments.clear()
            }
        } catch (_: NoSuchFileException) {
            data = linkedMapOf()
            comments.clear()
        }
        // ConfigException propagates — old data/comments untouched
    }

    override fun saveDefaults() {
        for ((path, value) in defaults) {
            if (resolve(path) == null) {
                setAtPath(path, value)
            }
        }
        try {
            configPath.parent?.let { Files.createDirectories(it) }
        } catch (e: IOException) {
            throw ConfigException(
                configPath,
                "Could not create directories for configuration file '$configPath'.\n" +
                    "Please check file system permissions.",
                e
            )
        }
        saveToFile()
    }

    override fun save() {
        saveToFile()
    }

    private fun saveToFile() {
        val representer = StandardRepresenter(yamlDumpSettings)
        val node = representer.represent(data)
        if (node is MappingNode) {
            applyComments(node)
        }
        val writer = StringStreamWriter()
        val dump = Dump(yamlDumpSettings)
        dump.dumpNode(node, writer)
        try {
            Files.writeString(configPath, writer.toString())
        } catch (e: IOException) {
            throw ConfigException(
                configPath,
                "Could not save configuration to '$configPath'. The in-memory configuration is still intact.\n" +
                    "Please check that the file is not read-only and that there is enough disk space.",
                e
            )
        }
    }

    private fun applyComments(node: MappingNode, prefix: String? = null) {
        for (tuple in node.value) {
            val keyNode = tuple.keyNode
            if (keyNode is ScalarNode) {
                val path = if (prefix != null) "$prefix.${keyNode.value}" else keyNode.value
                val pathComments = comments[path]
                if (pathComments != null) {
                    keyNode.blockComments = pathComments.map { commentText ->
                        CommentLine(Optional.empty(), Optional.empty(), " $commentText", CommentType.BLOCK)
                    }
                }
                val valueNode = tuple.valueNode
                if (valueNode is MappingNode) {
                    applyComments(valueNode, path)
                }
            }
        }
    }

    override fun getComments(path: String): List<String> = comments[path] ?: emptyList()

    override fun setComments(path: String, comments: List<String>) {
        this.comments[path] = comments
    }
}

private class StringStreamWriter : java.io.StringWriter(), StreamDataWriter {
    override fun write(str: String) = super<java.io.StringWriter>.write(str)
    override fun write(str: String, off: Int, len: Int) = super<java.io.StringWriter>.write(str, off, len)
    override fun flush() = super<java.io.StringWriter>.flush()
}
