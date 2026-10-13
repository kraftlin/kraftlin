package io.github.kraftlin.config

import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.api.LoadSettings
import org.snakeyaml.engine.v2.api.lowlevel.Compose
import org.snakeyaml.engine.v2.api.lowlevel.Parse
import org.snakeyaml.engine.v2.comments.CommentLine
import org.snakeyaml.engine.v2.comments.CommentType
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.events.CommentEvent
import org.snakeyaml.engine.v2.events.NodeEvent
import org.snakeyaml.engine.v2.exceptions.YamlEngineException
import org.snakeyaml.engine.v2.nodes.CollectionNode
import org.snakeyaml.engine.v2.nodes.MappingNode
import org.snakeyaml.engine.v2.nodes.Node
import org.snakeyaml.engine.v2.nodes.NodeTuple
import org.snakeyaml.engine.v2.nodes.ScalarNode
import org.snakeyaml.engine.v2.nodes.SequenceNode
import org.snakeyaml.engine.v2.nodes.Tag
import org.snakeyaml.engine.v2.representer.StandardRepresenter
import org.snakeyaml.engine.v2.schema.CoreSchema
import java.io.IOException
import java.nio.charset.CharacterCodingException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.*
import java.util.logging.Logger

internal val defaultLogger: Logger = Logger.getLogger("io.github.kraftlin.config")

internal val yamlLoadSettings: LoadSettings = LoadSettings.builder()
    .setParseComments(true)
    .setSchema(CoreSchema())
    .build()

/**
 * Writes block style with comments, aliases (`*name`) as copies of their value and long values on one line.
 * Values are read as copies, so a save writes them as copies instead of generating anchors for nodes kept from the file.
 */
internal val yamlSaveSettings: DumpSettings = DumpSettings.builder()
    .setDefaultFlowStyle(FlowStyle.BLOCK)
    .setDumpComments(true)
    .setSchema(CoreSchema())
    .setDereferenceAliases(true)
    .setSplitLines(false)
    .build()

/**
 * Writes multi-line strings double-quoted instead of as literal block (`|`), because a literal block does not load
 * back correctly when a line starts with `U+0085` (next line). Double quotes escape every line break.
 */
internal class YamlRepresenter : StandardRepresenter(yamlSaveSettings) {
    override fun representScalar(tag: Tag, value: String, style: ScalarStyle): Node =
        super.representScalar(tag, value, if (style == ScalarStyle.LITERAL) ScalarStyle.DOUBLE_QUOTED else style)
}

/**
 * Wraps an arbitrary YAML file for use in [AbstractConfig].
 *
 * Uses [snakeyaml-engine](https://github.com/snakeyaml/snakeyaml-engine) (YAML 1.2) internally.
 * This means only `true`/`false` are booleans — `yes`/`no`/`on`/`off` are treated as strings.
 *
 * Saving keeps the comments of the file: comments, blank lines and the quoting of every value that was not changed
 * programmatically are written back as loaded. A changed value keeps the comments of its key, a removed key loses its
 * comments. Comment lines at the top of the file that are separated from the first key by a blank line are the file
 * header and stay at the top. Comments of a key can also be read and replaced programmatically.
 * Indentation and line layout are normalized: nested entries are indented by two spaces, aliases (`*name`) are
 * written as copies of their value, strings with line breaks are written double-quoted on one line, and a flow mapping
 * (`{a: 1}`) that gets new keys or comments is written in block style.
 *
 * String getters and `getMap` return values as typed in the file: `1.10`, `007` and `True` are read as `"1.10"`, `"007"` and
 * `"True"`, while the numeric and boolean getters parse them. Values set programmatically are converted with
 * [toString].
 *
 * Type coercion is applied when reading values: lossless conversions (e.g. `Int` to `Long`, any scalar to `String`)
 * succeed silently. Lossy or impossible conversions log a warning and return the registered default.
 * A key that is present without a value reads as an empty list or map, and as the default for any other type.
 *
 * This implementation is not thread-safe. Callers must ensure that reads, writes, reloads, and saves
 * are not performed concurrently from multiple threads.
 *
 * @throws ConfigException if the file cannot be read (not UTF-8, no permission), contains invalid YAML or an alias
 *     that includes itself
 */
public fun wrapConfig(configPath: Path): AbstractConfig.ConfigWrapper =
    wrapConfig(configPath, defaultLogger)

/**
 * Wraps an arbitrary YAML file for use in [AbstractConfig].
 *
 * @param configPath Path to the YAML configuration file
 * @param logger Logger for warnings about invalid configuration values
 * @throws ConfigException if the file cannot be read (not UTF-8, no permission), contains invalid YAML or an alias
 *     that includes itself
 * @see wrapConfig
 */
public fun wrapConfig(configPath: Path, logger: Logger): AbstractConfig.ConfigWrapper =
    YamlConfigWrapper(configPath, logger)

/** A parsed configuration file. */
private class YamlFile(val root: MappingNode, val header: List<CommentLine>, val data: MutableMap<String, Any?>)

/** Marks a path that is not in the configuration, as opposed to a key present without a value (`null`). */
private object Absent

/** The key of a mapping entry. snakeyaml-engine rejects keys that are not scalars, so every key is a scalar. */
internal val NodeTuple.key: String
    get() = (keyNode as ScalarNode).value

/** Index of the first comment line that belongs to the key itself: the lines after the last blank line. */
private fun ownCommentsStart(comments: List<CommentLine>): Int =
    comments.indexOfLast { it.commentType == CommentType.BLANK_LINE } + 1

private fun commentLine(text: String): CommentLine =
    CommentLine(Optional.empty(), Optional.empty(), if (text.isEmpty()) "" else " $text", CommentType.BLOCK)

private val blankLine: CommentLine = CommentLine(Optional.empty(), Optional.empty(), "", CommentType.BLANK_LINE)

/** [value] as a [Boolean], or `null` if it is not `true` or `false` (in any case). */
private fun booleanOrNull(value: Any): Boolean? = when (value) {
    is Boolean -> value
    is String -> value.lowercase().toBooleanStrictOrNull()
    else -> null
}

/** [value] as an [Int], or `null` if it is not a whole number in range. */
private fun intOrNull(value: Any): Int? = when (value) {
    is Int -> value
    is Long -> if (value in Int.MIN_VALUE..Int.MAX_VALUE) value.toInt() else null
    is Double -> if (value % 1.0 == 0.0 && value >= Int.MIN_VALUE && value <= Int.MAX_VALUE) value.toInt() else null
    is String -> value.toIntOrNull()
    else -> null
}

/** [value] as a [Long], or `null` if it is not a whole number in range. */
private fun longOrNull(value: Any): Long? = when (value) {
    is Long -> value
    is Int -> value.toLong()
    // Long.MAX_VALUE is not exactly representable as a double, the closest double is out of range
    is Double -> if (value % 1.0 == 0.0 && value >= Long.MIN_VALUE.toDouble() && value < Long.MAX_VALUE.toDouble()) {
        value.toLong()
    } else {
        null
    }
    is String -> value.toLongOrNull()
    else -> null
}

/** [value] as a [Double], or `null` if it is not a number or not finite (`NaN`, infinity). */
private fun doubleOrNull(value: Any): Double? = when (value) {
    is Double -> value
    is Int -> value.toDouble()
    is Long -> value.toDouble()
    is String -> value.toDoubleOrNull()
    else -> null
}?.takeIf { it.isFinite() }

/** [value] as a [String], or `null` if it is a list or a section. */
private fun stringOrNull(value: Any): String? = if (value is List<*> || value is Map<*, *>) null else value.toString()

/**
 * Describes [value] for server admins: `a list`, `a section`, `empty`, or the value in quotes.
 * Pass values from the file as typed there, so that the quoted text can be found in the file.
 */
private fun describe(value: Any?): String = when (value) {
    null -> "empty"
    is List<*> -> "a list"
    is Map<*, *> -> "a section"
    else -> "'$value'"
}

/** Describes the value of [node] as typed in the file, see [describe]. */
private fun describe(node: Node): String = when (node) {
    is ScalarNode -> describe(if (node.tag == Tag.NULL) null else node.value)
    is SequenceNode -> "a list"
    else -> "a section"
}

/** Describes the default [value] for server admins: `empty` for an empty string, otherwise the value. */
private fun describeDefault(value: Any): String = if (value == "") "empty" else value.toString()

private class YamlConfigWrapper(
    private val configPath: Path,
    private val logger: Logger
) : AbstractConfig.ConfigWrapper {

    /** The node tree of the file as loaded, kept to write back its comments and formatting. */
    private var root: MappingNode? = null
    /** Comment lines at the top of the file, separated from the first key by a blank line. */
    private var header: List<CommentLine> = emptyList()
    private var data: MutableMap<String, Any?> = linkedMapOf()
    /** Paths written since load. Their nodes in [root] no longer match [data]. */
    private val writtenPaths: MutableSet<String> = mutableSetOf()
    private val defaults: MutableMap<String, Any?> = linkedMapOf()
    /** Comments set since load, replacing the comment lines of their key on save. */
    private val newComments: MutableMap<String, List<String>> = linkedMapOf()
    private val representer = YamlRepresenter()

    override fun warn(message: String) {
        logger.warning(message)
    }

    init {
        reloadConfig()
    }

    /**
     * Parses the YAML file without mutating any instance state.
     *
     * @return the parsed file, or `null` if it does not exist, is blank or is not a mapping
     * @throws ConfigException if the file cannot be read or contains invalid YAML
     */
    private fun parseFile(): YamlFile? {
        val yaml = readFile() ?: return null
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
            logger.warning(
                "Configuration file '$configPath' does not contain keys and values (found ${describe(node)}), using the defaults"
            )
            return null
        }

        val data = nodeToMap(node, null, hashSetOf(node))
        return YamlFile(node, takeHeader(node, yaml), data)
    }

    /** @return the file content, or `null` if the file does not exist */
    private fun readFile(): String? = try {
        Files.readString(configPath)
    } catch (_: NoSuchFileException) {
        null // File doesn't exist yet — start with empty data, defaults will be applied later
    } catch (e: CharacterCodingException) {
        throw ConfigException(
            configPath,
            "Could not read configuration file '$configPath' because it is not valid UTF-8.\n" +
                "Please save the file with UTF-8 encoding.",
            e
        )
    } catch (e: IOException) {
        throw ConfigException(
            configPath,
            "Could not read configuration file '$configPath'.\n" +
                "Please check that it is a regular file and that the server is allowed to read it.",
            e
        )
    }

    /**
     * Removes the file header from the comments of the first key and returns it: the comment lines up to the last
     * blank line. snakeyaml-engine drops the comments above a flow mapping and those of a file without keys, so these
     * are read from the parser events, followed by a blank line to keep them apart from keys added later.
     */
    private fun takeHeader(root: MappingNode, yaml: String): List<CommentLine> {
        if (root.value.isEmpty() || root.flowStyle == FlowStyle.FLOW) {
            val comments = Parse(yamlLoadSettings).parseString(yaml)
                .takeWhile { it !is NodeEvent }
                .filterIsInstance<CommentEvent>()
                .filter { it.commentType != CommentType.IN_LINE } // the comment behind `---`
                .map(::CommentLine)
            return if (comments.isEmpty() || comments.last().commentType == CommentType.BLANK_LINE) comments else comments + blankLine
        }
        val firstKey = root.value.first().keyNode
        val comments = firstKey.blockComments.orEmpty()
        val ownStart = ownCommentsStart(comments)
        firstKey.blockComments = comments.drop(ownStart)
        return comments.take(ownStart)
    }

    private fun nodeToMap(node: MappingNode, prefix: String?, ancestors: MutableSet<Node>): MutableMap<String, Any?> {
        val map = linkedMapOf<String, Any?>()
        val valueNodes = hashMapOf<String, Node>()
        for (tuple in node.value) {
            val path = if (prefix != null) "$prefix.${tuple.key}" else tuple.key
            valueNodes.put(tuple.key, tuple.valueNode)?.let { earlier ->
                logger.warning(
                    "Config value at '$path' is defined more than once, skipping the earlier one (${describe(earlier)})"
                )
            }
            map[tuple.key] = nodeToValue(tuple.valueNode, path, ancestors)
        }
        return map
    }

    /** @param ancestors the collections containing [node], to detect an alias that makes a collection contain itself */
    private fun nodeToValue(node: Node, path: String, ancestors: MutableSet<Node>): Any? {
        if (node is ScalarNode) return resolveScalar(node)
        if (!ancestors.add(node)) {
            throw ConfigException(
                configPath,
                "Could not read configuration file '$configPath' because the value at '$path' contains itself through an alias.\n" +
                    "Please replace the alias (*name) with a copy of the value."
            )
        }
        val value = when (node) {
            is MappingNode -> nodeToMap(node, path, ancestors)
            is SequenceNode -> node.value.map { nodeToValue(it, path, ancestors) }
            else -> null
        }
        ancestors.remove(node)
        return value
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

    /**
     * The value at [path], `null` if the key is present without a value, or [Absent].
     * Warns if [path] leads through a value that is not a section, e.g. `spring.enabled` with `spring: true`.
     */
    private fun resolve(path: String): Any? {
        val parts = path.split('.')
        var current: Any? = data
        for ((index, part) in parts.withIndex()) {
            if (current !is Map<*, *>) {
                if (current != null) {
                    val section = parts.subList(0, index).joinToString(".")
                    logger.warning(
                        "Config value at '$section' is not a section (found ${describe(asTyped(current, section))}), " +
                            "so '$path' cannot be read from it, " +
                            "using the default${defaults[path]?.let { " (${describeDefault(it)})" }.orEmpty()}"
                    )
                }
                return Absent
            }
            if (part !in current) return Absent
            current = current[part]
        }
        return current
    }

    /**
     * Like [resolve], but values unchanged since load are returned as typed in the file:
     * `1.10` stays `"1.10"` instead of becoming the double `1.1`.
     */
    private fun resolveAsTyped(path: String): Any? = resolve(path).let { if (it === Absent) it else asTyped(it, path) }

    /**
     * [value], the value at [path], with the scalars unchanged since load as typed in the file. Values written since
     * load, including single entries of a section, are returned as they are.
     *
     * @param node the node of [value] in the file as loaded, by default looked up by [path], or `null` if it is not in
     *     the file
     */
    private fun asTyped(value: Any?, path: String, node: Node? = loadedTuple(path)?.valueNode): Any? = when {
        node == null -> value
        node is MappingNode && value is Map<*, *> -> {
            val tuples = node.value.associateBy { it.key } // for duplicate keys the last one, which holds the value
            value.mapValues { (key, entryValue) -> asTyped(entryValue, "$path.$key", tuples[key.toString()]?.valueNode) }
        }
        value == null || !isUnchanged(path) -> value
        node is ScalarNode -> node.value
        node is SequenceNode && value is List<*> ->
            node.value.zip(value) { element, elementValue -> asTyped(elementValue, path, element) }
        else -> value
    }

    /** Whether neither [path] nor a path above or below it was written since load. */
    private fun isUnchanged(path: String): Boolean =
        writtenPaths.none { it == path || it.startsWith("$path.") || path.startsWith("$it.") }

    /** The entry at [path] in the file as loaded. For duplicate keys, the last one, which holds the value. */
    private fun loadedTuple(path: String): NodeTuple? {
        var mapping: MappingNode? = root
        var tuple: NodeTuple? = null
        for (part in path.split('.')) {
            tuple = mapping?.value?.lastOrNull { it.key == part } ?: return null
            mapping = tuple.valueNode as? MappingNode
        }
        return tuple
    }

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
        // Not a change: the value keeps its node from the file, with its quoting and comments
        if (value != null && current[parts.last()] == value) return
        writtenPaths += path
        if (value == null) {
            current.remove(parts.last())
        } else {
            current[parts.last()] = value
        }
    }

    /**
     * [value] converted by [convert]. Without a value, the registered default is converted, or [fallback] is used if
     * there is none. A value that cannot be converted is reported and the default is used instead.
     *
     * @param value the resolved value at [path]
     */
    private inline fun <reified T : Any> convertValue(value: Any?, path: String, fallback: T, convert: (Any) -> T?): T {
        val default = defaults[path] as? T ?: fallback
        val present = value != null && value !== Absent
        val actual = (if (present) value else defaults[path]) ?: return default
        return convert(actual) ?: default.also {
            val found = if (present) asTyped(actual, path) else actual
            logger.warning(
                "Config value at '$path' is not a valid ${T::class.simpleName} (found ${describe(found)}), " +
                    "using the default (${describeDefault(default)})"
            )
        }
    }

    /**
     * [value] as a list with its elements converted by [convert]. Elements that cannot be converted are reported and
     * skipped.
     *
     * @param value the resolved value: a key present without a value reads as an empty list
     */
    private inline fun <reified T : Any> convertList(value: Any?, path: String, convert: (Any) -> T?): List<T> {
        val list = when (value) {
            Absent -> defaults[path] ?: return emptyList()
            null -> return emptyList()
            else -> value
        }
        val fromDefault = value === Absent
        if (list !is List<*>) {
            @Suppress("UNCHECKED_CAST")
            val default = defaults[path] as? List<T> ?: emptyList()
            val found = if (fromDefault) list else asTyped(list, path)
            logger.warning(
                "Config value at '$path' is not a list (found ${describe(found)}), using the default ($default)"
            )
            return default
        }
        return list.mapIndexedNotNull { index, element ->
            element?.let(convert) ?: run {
                val found = if (fromDefault) element else (asTyped(list, path) as List<*>)[index]
                logger.warning(
                    "Config list element at '$path' is not a valid ${T::class.simpleName} " +
                        "(found ${describe(found)}), skipping"
                )
                null
            }
        }
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

    override fun getBoolean(path: String): Boolean = convertValue(resolve(path), path, false, ::booleanOrNull)

    override fun getInt(path: String): Int = convertValue(resolve(path), path, 0, ::intOrNull)

    override fun getLong(path: String): Long = convertValue(resolve(path), path, 0L, ::longOrNull)

    override fun getDouble(path: String): Double = convertValue(resolve(path), path, 0.0, ::doubleOrNull)

    override fun getString(path: String): String = convertValue(resolveAsTyped(path), path, "", ::stringOrNull)

    override fun getBooleanList(path: String): List<Boolean> = convertList(resolve(path), path, ::booleanOrNull)

    override fun getIntegerList(path: String): List<Int> = convertList(resolve(path), path, ::intOrNull)

    override fun getLongList(path: String): List<Long> = convertList(resolve(path), path, ::longOrNull)

    override fun getDoubleList(path: String): List<Double> = convertList(resolve(path), path, ::doubleOrNull)

    override fun getStringList(path: String): List<String> = convertList(resolveAsTyped(path), path, ::stringOrNull)

    override fun getMap(path: String): Map<String, Any> {
        val map = when (val value = resolveAsTyped(path)) {
            Absent -> defaults[path] ?: return emptyMap()
            null -> return emptyMap() // present without a value: an empty map
            else -> value
        }
        val entries = map as? Map<*, *> ?: run {
            val default = defaults[path] as? Map<*, *> ?: emptyMap<String, Any>()
            logger.warning(
                "Config value at '$path' is not a section (found ${describe(map)}), using the default ($default)"
            )
            default
        }
        val result = linkedMapOf<String, Any>()
        for ((key, value) in entries) {
            val text = value?.let(::stringOrNull)
            if (text != null) {
                result[key.toString()] = text
            } else {
                logger.warning(
                    "Config value at '$path.$key' is not a valid String (found ${describe(value)}), skipping"
                )
            }
        }
        return result
    }

    override fun reloadConfig() {
        val file = parseFile()
        // Parse succeeded — safe to swap. A ConfigException propagates and leaves the old state untouched.
        root = file?.root
        header = file?.header.orEmpty()
        data = file?.data ?: linkedMapOf()
        writtenPaths.clear()
        newComments.clear()
    }

    override fun saveDefaults() {
        for ((path, value) in defaults) {
            when (resolve(path)) {
                Absent -> setAtPath(path, value)
                // Present without a value: a list or map reads as empty, so write that instead of the default
                null -> setAtPath(
                    path,
                    when (value) {
                        is List<*> -> emptyList<Any>()
                        is Map<*, *> -> emptyMap<String, Any>()
                        else -> value
                    }
                )
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
        val node = mappingNode(data, root, null)
        // Blank lines above the first key separated it from a removed key, the header ends with one already
        node.value.firstOrNull()?.keyNode?.let { key ->
            key.blockComments = key.blockComments.orEmpty().dropWhile { it.commentType == CommentType.BLANK_LINE }
        }
        node.blockComments = header + node.blockComments.orEmpty()
        val writer = StringStreamWriter()
        val dump = Dump(yamlSaveSettings)
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

    /**
     * Builds the node for [values], the mapping at [prefix]. Entries unchanged since load keep their nodes from
     * [original], so they are written back with their comments and formatting.
     */
    private fun mappingNode(values: Map<*, *>, original: MappingNode?, prefix: String?): MappingNode {
        // For duplicate keys the last entry holds the value, the others are dropped
        val originalTuples = original?.value.orEmpty().associateBy { it.key }
        val tuples = values.map { (key, value) ->
            val path = if (prefix != null) "$prefix.$key" else key.toString()
            val tuple = originalTuples[key.toString()]
            entry(keyNode(key, tuple?.keyNode as ScalarNode?, path), valueNode(value, tuple?.valueNode, path), tuple?.valueNode)
        }
        // Comments cannot be written inside a flow mapping, so a mapping is written in block style if it gets new keys,
        // which may come with comments, has keys with comments or contains a block mapping
        val blockNeeded = values.keys.any { it.toString() !in originalTuples } || tuples.any {
            !it.keyNode.blockComments.isNullOrEmpty() || (it.valueNode as? MappingNode)?.flowStyle == FlowStyle.BLOCK
        }
        val flowStyle = if (original != null && !blockNeeded) original.flowStyle else FlowStyle.BLOCK
        return MappingNode(Tag.MAP, tuples, flowStyle).also {
            it.blockComments = original?.blockComments
            if (flowStyle == original?.flowStyle) it.inLineComments = original.inLineComments // otherwise moved to the key
            it.endComments = original?.endComments
        }
    }

    /**
     * The entry of [keyNode] and [valueNode], with the in-line comment kept on the line of the key if the value moved
     * between that line and the lines below: `limits: {min: 1} # bounds` written in block style becomes
     * `limits: # bounds`, and `section: # about` with its section replaced by `x` becomes `section: x # about`.
     * A value node kept from the file is written as loaded.
     */
    private fun entry(keyNode: Node, valueNode: Node, originalValue: Node?): NodeTuple {
        if (originalValue is MappingNode && valueNode is MappingNode && valueNode.flowStyle != originalValue.flowStyle) {
            keyNode.inLineComments = originalValue.inLineComments
        } else if (valueNode !== originalValue && !keyNode.inLineComments.isNullOrEmpty() && isWrittenOnOneLine(valueNode)) {
            valueNode.inLineComments = keyNode.inLineComments
            keyNode.inLineComments = null
        }
        return NodeTuple(keyNode, valueNode)
    }

    /** A copy of the [original] key node, or a new one, with its own comment lines replaced if set since load. */
    private fun keyNode(key: Any?, original: ScalarNode?, path: String): Node {
        val node = original?.let {
            ScalarNode(it.tag, it.isResolved, it.value, it.scalarStyle, it.startMark, it.endMark)
                .apply { inLineComments = it.inLineComments }
        } ?: representer.represent(key)
        val comments = original?.blockComments.orEmpty()
        val ownStart = ownCommentsStart(comments)
        node.blockComments = comments.take(ownStart) + (newComments[path]?.map(::commentLine) ?: comments.drop(ownStart))
        return node
    }

    /** The [original] value node if the value at [path] is unchanged since load, otherwise a new one for [value]. */
    private fun valueNode(value: Any?, original: Node?, path: String): Node = when {
        value is Map<*, *> -> mappingNode(value, original as? MappingNode, path)
        original != null && isUnchanged(path) -> original
        else -> representer.represent(value).also { node ->
            if (original != null && isWrittenOnOneLine(node)) {
                node.inLineComments = original.inLineComments
            }
        }
    }

    /**
     * Whether [node], built for a value set since load, is written on one line, so that an in-line comment can follow
     * it.
     */
    private fun isWrittenOnOneLine(node: Node): Boolean = when (node) {
        is ScalarNode -> true // YamlRepresenter writes no block scalars and lines are not split
        is CollectionNode<*> -> node.value.isEmpty() // written as [] or {}
        else -> false
    }

    override fun getComments(path: String): List<String> {
        newComments[path]?.let { return it }
        val comments = loadedTuple(path)?.keyNode?.blockComments.orEmpty()
        return comments.drop(ownCommentsStart(comments)).map { it.value.removePrefix(" ") }
    }

    override fun setComments(path: String, comments: List<String>) {
        newComments[path] = comments.flatMap { it.lines() }
    }
}

private class StringStreamWriter : java.io.StringWriter(), StreamDataWriter {
    override fun write(str: String) = super<java.io.StringWriter>.write(str)
    override fun write(str: String, off: Int, len: Int) = super<java.io.StringWriter>.write(str, off, len)
    override fun flush() = super<java.io.StringWriter>.flush()
}
