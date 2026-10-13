package io.github.kraftlin.config.paper

import io.github.kraftlin.config.AbstractConfig
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import java.util.EnumSet
import java.util.Locale

public abstract class AbstractBukkitConfig(configWrapper: ConfigWrapper) : AbstractConfig(configWrapper) {

    /**
     * Config delegate for a set of materials. Supports minecraft item keys with or without 'minecraft:' prefix
     * such as '#minecraft:stone' or 'stone' and item tags such as '#minecraft:shulker_boxes'.
     *
     * @param path A dot separated path for the config value
     * @param default The default value for this config entry. Only [Material] and [Tag] instances allowed.
     * @param comments Comment lines written above the entry
     */
    @JvmName("configSetMaterial")
    protected fun config(path: String, default: Collection<Keyed>, vararg comments: String): ConfigDelegate<Set<Material>> {
        configWrapper.addDefault(path, serializeMaterialAndTags(default))
        return ConfigDelegate(
            path,
            ::readMaterials,
            { pathInner, value -> configWrapper.set(pathInner, serializeMaterialAndTags(value)) },
            comments.toList()
        )
    }

    private fun readMaterials(path: String): Set<Material> {
        val materials = EnumSet.noneOf(Material::class.java)
        for (element in configWrapper.getStringList(path)) {
            val matched = matchMaterialOrTag(element)
            if (matched == null) {
                configWrapper.warn("Config list element at '$path' is not a known material or tag ('$element'), skipping")
            } else {
                materials += matched
            }
        }
        return materials
    }
}

private fun serializeMaterialAndTags(input: Collection<Keyed>): List<String> {
    require(input.all { it is Material || it is Tag<*> }) { "Only Material or Tag instances allowed" }
    val serialized = input.map { if (it is Material) it.key.toString() else "#${it.key}" }
    return serialized.sorted()
}

/** The materials named by [input], or null if it is neither a known material nor a known tag. */
private fun matchMaterialOrTag(input: String): Collection<Material>? {
    return if (input.trim().startsWith('#')) {
        matchMaterialTag(input)?.values
    } else {
        Material.matchMaterial(input)?.let(::setOf)
    }
}

private fun matchMaterialTag(input: String): Tag<Material>? {
    val cleanInput = input.lowercase(Locale.ENGLISH).replace(Regex("[^a-z0-9._:-]"), "")
    if (!cleanInput.matches(Regex("([a-z0-9._-]+:)?([a-z0-9._-]+)"))) {
        return null
    }
    val split = cleanInput.split(":")
    val namespacedKey = if (split.size > 1) {
        @Suppress("DEPRECATION")
        (NamespacedKey(split[0], split[1]))
    } else {
        NamespacedKey.minecraft(cleanInput)
    }
    return Bukkit.getTag(Tag.REGISTRY_BLOCKS, namespacedKey, Material::class.java)
        ?: Bukkit.getTag(Tag.REGISTRY_ITEMS, namespacedKey, Material::class.java)
}
