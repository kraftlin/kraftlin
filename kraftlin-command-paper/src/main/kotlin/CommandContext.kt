package io.github.kraftlin.command.paper

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/** The [CommandSender] who executed the command. */
public val PaperContext.sender: CommandSender
    get() = rawContext.source.sender

/** Returns the sender as a [Player], or throws a command error if the sender is not a player. */
public fun PaperContext.requirePlayer(): Player {
    val s: CommandSender = sender
    if (s is Player) return s
    throw SimpleCommandExceptionType(LiteralMessage("Only players can use this command.")).create()
}
