package io.github.kbrigx.paper

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

public val PaperContext.sender: CommandSender
    get() = rawContext.source.sender

public fun PaperContext.requirePlayer(): Player {
    val s: CommandSender = sender
    if (s is Player) return s
    throw SimpleCommandExceptionType(LiteralMessage("Only players can use this command.")).create()
}
