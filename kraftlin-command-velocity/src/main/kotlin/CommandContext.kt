package io.github.kraftlin.command.velocity

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player

public val VelocityContext.sender: CommandSource
    get() = rawContext.source

public fun VelocityContext.requirePlayer(): Player {
    val s: CommandSource = sender
    if (s is Player) return s
    throw SimpleCommandExceptionType(LiteralMessage("Only players can use this command.")).create()
}
