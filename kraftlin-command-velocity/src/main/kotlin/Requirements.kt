package io.github.kraftlin.command.velocity

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import io.github.kraftlin.command.requires

/** Requires the sender to have the given [permission]. */
public fun VelocityLiteralNode.requiresPermission(permission: String): Unit =
    requires { it.hasPermission(permission) }

/** Requires the sender to be a [Player]. */
public fun VelocityLiteralNode.requiresPlayer(): Unit =
    requires { it is Player }

/** Requires the sender to match a [predicate]. */
public fun VelocityLiteralNode.requiresSender(predicate: (CommandSource) -> Boolean): Unit =
    requires { predicate(it) }
