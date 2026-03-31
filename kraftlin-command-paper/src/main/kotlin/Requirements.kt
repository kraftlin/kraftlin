package io.github.kraftlin.command.paper

import io.github.kraftlin.command.requires
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/** Requires the sender to have the given [permission]. */
public fun PaperLiteralNode.requiresPermission(permission: String): Unit =
    requires { it.sender.hasPermission(permission) }

/** Requires the sender to be a [Player]. */
public fun PaperLiteralNode.requiresPlayer(): Unit =
    requires { it.sender is Player }

/** Requires the sender to match a [predicate]. */
public fun PaperLiteralNode.requiresSender(predicate: (CommandSender) -> Boolean): Unit =
    requires { predicate(it.sender) }
