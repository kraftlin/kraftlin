package io.github.kraftlin.config.paper

import io.github.kraftlin.config.AbstractConfig
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import java.util.Locale

public abstract class AbstractBukkitConfig(configWrapper: ConfigWrapper) : AbstractConfig(configWrapper) {

    /**
     * Config delegate for a [MaterialSet]. The file lists materials by their key with or without 'minecraft:' prefix,
     * such as 'minecraft:stone' or 'stone', and block or item tags such as '#minecraft:shulker_boxes' or
     * '#mineable/pickaxe'. Tags are read as [MaterialSet.tags], resolved on every (re)load, and written back as tags.
     * Unknown entries and legacy materials are skipped with a warning.
     *
     * Any collection of materials is accepted as default, e.g. `listOf(Material.STONE)`. Pass a [MaterialSet] to
     * write tags as defaults too. For a plain [List] of materials, give the type argument:
     * `config<Material>("path", listOf(Material.STONE))`.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry. Its tags are written as well if it is a [MaterialSet].
     * @param comments Comment lines written above the entry (applied by saveDefaults if the entry has none)
     */
    @JvmName("configMaterialSet")
    protected fun config(
        path: String,
        default: Collection<Material>,
        vararg comments: String
    ): ConfigDelegate<MaterialSet> {
        configWrapper.addDefault(path, serializeMaterials(default))
        return ConfigDelegate(
            path,
            ::readMaterials,
            { pathInner, value -> configWrapper.set(pathInner, serializeMaterials(value)) },
            comments.toList()
        )
    }

    /**
     * Keeps plugins compiled against 0.8.0 working. Reads like the [MaterialSet] delegate.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry. Only [Material] and [Tag] of [Material] allowed.
     */
    @Deprecated("Use config(path, Collection<Material>) which returns a MaterialSet", level = DeprecationLevel.HIDDEN)
    @JvmName("configSetMaterial")
    protected fun config(path: String, default: Collection<Keyed>): ConfigDelegate<Set<Material>> {
        configWrapper.addDefault(path, serializeMaterials(default.toMaterialSet()))
        return ConfigDelegate(
            path,
            ::readMaterials,
            { pathInner, value -> configWrapper.set(pathInner, serializeMaterials(value)) }
        )
    }

    private fun readMaterials(path: String): MaterialSet {
        val materials = mutableListOf<Material>()
        val tags = mutableListOf<Tag<Material>>()
        for (element in configWrapper.getStringList(path)) {
            val matched = if (element.trim().startsWith('#')) {
                matchMaterialTag(element)?.also { tags += it }
            } else {
                Material.matchMaterial(element)?.takeUnless { it.isLegacy }?.also { materials += it }
            }
            if (matched == null) {
                configWrapper.warn("Config list element at '$path' is not a known material or tag ('$element'), skipping")
            }
        }
        return MaterialSet(materials, tags)
    }
}

private fun Collection<Keyed>.toMaterialSet(): MaterialSet {
    require(all { it is Material || it is Tag<*> && it.values.all { value -> value is Material } }) {
        "Only Material or Tag<Material> instances allowed"
    }
    return MaterialSet(filterIsInstance<Material>(), filterIsInstance<Tag<Material>>())
}

/** The config entries for [input]: materials by key and, if it is a [MaterialSet], its tags as '#' and key. */
private fun serializeMaterials(input: Collection<Material>): List<String> {
    val materialSet = input as? MaterialSet ?: MaterialSet(input)
    val serialized = materialSet.materials.map { it.key.toString() } + materialSet.tags.map { "#${it.key}" }
    return serialized.sorted()
}

private fun matchMaterialTag(input: String): Tag<Material>? {
    val cleanInput = input.lowercase(Locale.ENGLISH).replace(Regex("[^a-z0-9._:/-]"), "")
    if (!cleanInput.matches(Regex("([a-z0-9._-]+:)?([a-z0-9._/-]+)"))) {
        return null
    }
    val split = cleanInput.split(":")
    val namespacedKey = if (split.size > 1) {
        NamespacedKey(split[0], split[1])
    } else {
        NamespacedKey.minecraft(cleanInput)
    }
    return Bukkit.getTag(Tag.REGISTRY_BLOCKS, namespacedKey, Material::class.java)
        ?: Bukkit.getTag(Tag.REGISTRY_ITEMS, namespacedKey, Material::class.java)
}
