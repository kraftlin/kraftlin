package io.github.kraftlin.command.velocity

import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import io.github.kraftlin.command.requires

public fun VelocityLiteralNode.requiresPermission(permission: String): Unit =
    requires { it.hasPermission(permission) }

public fun VelocityLiteralNode.requiresPlayer(): Unit =
    requires { it is Player }

public fun VelocityLiteralNode.requiresSender(predicate: (CommandSource) -> Boolean): Unit =
    requires { predicate(it) }
