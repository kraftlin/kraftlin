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
     */
    @JvmName("configSetMaterial")
    protected fun config(path: String, default: Collection<Keyed>): ConfigDelegate<Set<Material>> {
        configWrapper.addDefault(path, serializeMaterialAndTags(default))
        return ConfigDelegate(
            path,
            { pathInner -> EnumSet.copyOf(configWrapper.getStringList(pathInner).flatMap(::matchMaterialOrTag)) },
            { pathInner, value -> configWrapper.set(pathInner, serializeMaterialAndTags(value)) },
        )
    }
}

private fun serializeMaterialAndTags(input: Collection<Keyed>): List<String> {
    require(input.all { it is Material || it is Tag<*> }) { "Only Material or Tag instances allowed" }
    val serialized = input.map { if (it is Material) it.key.toString() else "#${it.key}" }
    return serialized.sorted()
}

private fun matchMaterialOrTag(input: String): Collection<Material> {
    return if (input.trim().startsWith('#')) {
        matchMaterialTag(input)?.values ?: emptySet()
    } else {
        val material = Material.matchMaterial(input)
        if (material != null) setOf(material) else emptySet()
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
