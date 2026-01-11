package io.github.kbrigx.paper.arguments

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.github.kbrigx.core.argument
import io.github.kbrigx.paper.PaperArgumentNode
import io.github.kbrigx.paper.PaperContext
import io.github.kbrigx.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import org.bukkit.entity.Entity

/**
 * Adds an entity selector argument that must resolve to exactly one entity.
 *
 * Documentation: [Entity Argument](https://docs.papermc.io/paper/dev/command-api/arguments/entity-player/#entity-argument)
 *
 * @see [PaperContext.entity]
 */
public fun PaperLiteralNode.entity(
    name: String,
    block: PaperArgumentNode<EntitySelectorArgumentResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.entity(), block)

/**
 * Returns exactly one selected entity. Safe to use with [PaperLiteralNode.entity].
 *
 * Documentation: [Entity Argument](https://docs.papermc.io/paper/dev/command-api/arguments/entity-player/#entity-argument)
 * @see PaperLiteralNode.entity
 */
public fun PaperContext.entity(name: String): Entity {
    val list: List<Entity> = entities(name)
    return when (list.size) {
        1 -> list[0]
        0 -> throw SimpleCommandExceptionType(LiteralMessage("Entity not found.")).create()
        else -> throw SimpleCommandExceptionType(LiteralMessage("Too many entities matched.")).create()
    }
}

/**
 * Adds a selector argument that may resolve to zero or more entities.
 *
 * Documentation: [Entities Argument](https://docs.papermc.io/paper/dev/command-api/arguments/entity-player/#entities-argument)
 * @see [PaperContext.entities]
 */
public fun PaperLiteralNode.entities(
    name: String,
    block: PaperArgumentNode<EntitySelectorArgumentResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.entities(), block)

/**
 * Returns all entities matched by the entity selector (can be empty).
 *
 * Documentation: [Entities Argument](https://docs.papermc.io/paper/dev/command-api/arguments/entity-player/#entities-argument)
 * @see [PaperLiteralNode.entities]
 */
public fun PaperContext.entities(name: String): List<Entity> {
    val resolver: EntitySelectorArgumentResolver =
        rawContext.getArgument(name, EntitySelectorArgumentResolver::class.java)
    return resolver.resolve(rawContext.source)
}
