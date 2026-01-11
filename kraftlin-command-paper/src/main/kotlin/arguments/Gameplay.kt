package io.github.kbrigx.paper.arguments

import io.github.kbrigx.core.argument
import io.github.kbrigx.paper.PaperArgumentNode
import io.github.kbrigx.paper.PaperContext
import io.github.kbrigx.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import org.bukkit.GameMode
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot


public fun PaperLiteralNode.gameMode(
    name: String,
    block: PaperArgumentNode<GameMode>.() -> Unit,
): Unit = argument(name, ArgumentTypes.gameMode(), block)

public fun PaperContext.gameMode(name: String): GameMode = rawContext.getArgument<GameMode>(name, GameMode::class.java)


public fun PaperLiteralNode.objectiveCriteria(
    name: String,
    block: PaperArgumentNode<Criteria>.() -> Unit,
): Unit = argument(name, ArgumentTypes.objectiveCriteria(), block)

public fun PaperContext.objectiveCriteria(name: String): Criteria =
    rawContext.getArgument<Criteria>(name, Criteria::class.java)


public fun PaperLiteralNode.scoreboardDisplaySlot(
    name: String,
    block: PaperArgumentNode<DisplaySlot>.() -> Unit,
): Unit = argument(name, ArgumentTypes.scoreboardDisplaySlot(), block)

public fun PaperContext.scoreboardDisplaySlot(name: String): DisplaySlot =
    rawContext.getArgument<DisplaySlot>(name, DisplaySlot::class.java)
