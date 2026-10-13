package io.github.kraftlin.config.paper

import org.bukkit.Material
import org.bukkit.Tag
import java.util.Collections
import java.util.EnumSet

/**
 * An immutable set of materials that remembers which of them were given as single [materials] and which through
 * [tags], so that tags stay tags when the set is written to a config file.
 *
 * The set contains the [materials] and the values of all [tags]. The tags are resolved when the set is created: a set
 * created again later, e.g. when the config is reloaded, includes the blocks that new Minecraft versions or datapacks
 * add to a tag.
 *
 * Equality follows the [Set] contract: a [MaterialSet] equals every set with the same materials. Tags are not part of
 * equality, so a set with the tag `#minecraft:logs` equals a set that lists all logs as single materials.
 *
 * The `+` and `-` operators return a new [MaterialSet] and keep its tags:
 * - `+` adds materials to [materials] and tags to [tags]. Adding another [MaterialSet] adds its materials and tags.
 * - `-` removes materials from [materials] and tags (matched by key) from [tags]. A material that is not part of the
 *   set is ignored. Removing a material that one of the [tags] includes throws an [IllegalArgumentException], because
 *   the result could not keep the tag; remove the tag instead. Removing another [MaterialSet] removes its tags first,
 *   then its materials.
 *
 * @param materials The single materials of this set
 * @param tags The tags of this set. Of several tags with the same key, only the first is kept.
 * @throws IllegalArgumentException if [materials] contains legacy materials
 */
public class MaterialSet(
    materials: Collection<Material> = emptySet(),
    tags: Collection<Tag<Material>> = emptySet(),
) : AbstractSet<Material>() {

    /** The single materials of this set, without the values of its [tags]. */
    public val materials: Set<Material> = run {
        val legacy = materials.filter { it.isLegacy }
        require(legacy.isEmpty()) { "Legacy materials are not supported: ${legacy.joinToString { it.name }}" }
        Collections.unmodifiableSet(EnumSet.noneOf(Material::class.java).apply { addAll(materials) })
    }

    /** The tags of this set in the order they were given, at most one per key. */
    public val tags: Set<Tag<Material>> =
        Collections.unmodifiableSet(tags.distinctBy { it.key }.toCollection(LinkedHashSet()))

    /** The materials and the values of all tags at the time this set was created. */
    private val contents: Set<Material> = EnumSet.noneOf(Material::class.java).let { contents ->
        contents.addAll(this.materials)
        this.tags.forEach { contents.addAll(it.values) }
        Collections.unmodifiableSet(contents)
    }

    override val size: Int
        get() = contents.size

    override fun iterator(): Iterator<Material> = contents.iterator()

    override fun contains(element: Material): Boolean = element in contents

    /**
     * The tags of this set that contain [material], in the order of [tags]. Empty if [material] is not part of any tag,
     * even if it is one of the [materials].
     */
    public fun tagsContaining(material: Material): Set<Tag<Material>> =
        tags.filterTo(LinkedHashSet()) { material in it.values }

    /** A set with [element] added to the [materials] of this set. */
    public operator fun plus(element: Material): MaterialSet = MaterialSet(materials + element, tags)

    /**
     * A set with [elements] added to the [materials] of this set. If [elements] is a [MaterialSet], its materials are
     * added to the [materials] and its tags to the [tags] of this set.
     */
    public operator fun plus(elements: Iterable<Material>): MaterialSet =
        if (elements is MaterialSet) {
            MaterialSet(materials + elements.materials, tags + elements.tags)
        } else {
            MaterialSet(materials + elements, tags)
        }

    /** A set with [elements] added to the [materials] of this set. */
    public operator fun plus(elements: Array<out Material>): MaterialSet = MaterialSet(materials + elements, tags)

    /** A set with [elements] added to the [materials] of this set. */
    public operator fun plus(elements: Sequence<Material>): MaterialSet = MaterialSet(materials + elements, tags)

    /** A set with [tag] added to the [tags] of this set. Nothing changes if a tag with the same key is part of it. */
    public operator fun plus(tag: Tag<Material>): MaterialSet = MaterialSet(materials, tags + tag)

    /**
     * A set without [element]. Nothing changes if [element] is not part of this set.
     *
     * @throws IllegalArgumentException if one of the [tags] includes [element]
     */
    public operator fun minus(element: Material): MaterialSet = without(listOf(element))

    /**
     * A set without [elements]. Elements that are not part of this set are ignored. If [elements] is a [MaterialSet],
     * its tags are removed from the [tags] of this set first, then its materials.
     *
     * @throws IllegalArgumentException if one of the remaining [tags] includes one of the [elements]; nothing is
     *     removed
     */
    public operator fun minus(elements: Iterable<Material>): MaterialSet =
        if (elements is MaterialSet) without(elements.materials, elements.tags) else without(elements.toList())

    /**
     * A set without [elements]. Elements that are not part of this set are ignored.
     *
     * @throws IllegalArgumentException if one of the [tags] includes one of the [elements]; nothing is removed
     */
    public operator fun minus(elements: Array<out Material>): MaterialSet = without(elements.asList())

    /**
     * A set without [elements]. Elements that are not part of this set are ignored.
     *
     * @throws IllegalArgumentException if one of the [tags] includes one of the [elements]; nothing is removed
     */
    public operator fun minus(elements: Sequence<Material>): MaterialSet = without(elements.toList())

    /** A set without the tag with the key of [tag]. Nothing changes if no such tag is part of this set. */
    public operator fun minus(tag: Tag<Material>): MaterialSet = without(emptyList(), listOf(tag))

    /**
     * Shows the [materials] and [tags] of this set, e.g.
     * `MaterialSet(materials=[minecraft:stone], tags=[#minecraft:logs])`.
     */
    override fun toString(): String =
        "MaterialSet(materials=${materials.map { it.key.toString() }}, tags=${tags.map { "#${it.key}" }})"

    private fun without(
        removedMaterials: Collection<Material>,
        removedTags: Collection<Tag<Material>> = emptyList()
    ): MaterialSet {
        val removedTagKeys = removedTags.mapTo(HashSet()) { it.key }
        // Checks the current tag values, not the contents of this set: the result resolves the tags again
        for (material in removedMaterials) {
            val including = tagsContaining(material).filter { it.key !in removedTagKeys }
            require(including.isEmpty()) {
                "${material.name} is included by tag ${including.joinToString { "#${it.key}" }}; remove the tag instead"
            }
        }
        return MaterialSet(materials - removedMaterials.toSet(), tags.filter { it.key !in removedTagKeys })
    }
}

/** A [MaterialSet] of the given [materials], without tags. */
public fun materialSetOf(vararg materials: Material): MaterialSet = MaterialSet(materials.asList())
