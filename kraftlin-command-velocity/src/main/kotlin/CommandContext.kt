package io.github.kraftlin.command.velocity

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player

/** The [CommandSource] who executed the command. */
public val VelocityContext.sender: CommandSource
    get() = rawContext.source

/** Returns the sender as a [Player], or throws a command error if the sender is not a player. */
public fun VelocityContext.requirePlayer(): Player {
    val s: CommandSource = sender
    if (s is Player) return s
    throw SimpleCommandExceptionType(LiteralMessage("Only players can use this command.")).create()
}
