package io.github.kbrigx.paper.arguments

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.github.kbrigx.core.argument
import io.github.kbrigx.paper.PaperArgumentNode
import io.github.kbrigx.paper.PaperContext
import io.github.kbrigx.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player

/**
 * Adds an entity selector argument that must resolve to exactly one entity.
 *
 * Documentation: [Player Argument](https://docs.papermc.io/paper/dev/command-api/arguments/entity-player/#player-argument)
 * @see [PaperContext.player]
 */
public fun PaperLiteralNode.player(
    name: String,
    block: PaperArgumentNode<PlayerSelectorArgumentResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.player(), block)

/**
 * Returns exactly one selected player. Safe to use with [PaperLiteralNode.player].
 *
 * Documentation: [Player Argument](https://docs.papermc.io/paper/dev/command-api/arguments/entity-player/#player-argument)
 * @see PaperLiteralNode.player
 */
public fun PaperContext.player(name: String): Player {
    val list: List<Player> = players(name)
    return when (list.size) {
        1 -> list[0]
        0 -> throw SimpleCommandExceptionType(LiteralMessage("Player not found.")).create()
        else -> throw SimpleCommandExceptionType(LiteralMessage("Too many players matched.")).create()
    }
}

/**
 * Adds a selector argument that may resolve to zero or more players.
 *
 * Documentation: [Players Argument](https://docs.papermc.io/paper/dev/command-api/arguments/entity-player/#players-argument)
 * @see [PaperContext.players]
 */
public fun PaperLiteralNode.players(
    name: String,
    block: PaperArgumentNode<PlayerSelectorArgumentResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.players(), block)

/**
 * Returns all players matched by the player selector (can be empty).
 *
 * Documentation: [Players Argument](https://docs.papermc.io/paper/dev/command-api/arguments/entity-player/#players-argument)
 * @see [PaperLiteralNode.players]
 */
public fun PaperContext.players(name: String): List<Player> {
    val resolver: PlayerSelectorArgumentResolver =
        rawContext.getArgument(name, PlayerSelectorArgumentResolver::class.java)
    return resolver.resolve(rawContext.source)
}
