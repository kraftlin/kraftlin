package io.github.kraftlin.command.paper

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.Plugin


/**
 * Registers one or more Kraftlin commands for this plugin.
 *
 * This function hooks into Paper's `LifecycleEvents.COMMANDS` phase and registers
 * the provided commands when the server builds its command graph.
 *
 * This is intended to be called during plugin initialization (e.g. in `onEnable`).
 * Calling it multiple times is supported but will register multiple lifecycle handlers
 * and is therefore unnecessary in most cases.
 *
 * @param commands one or more Kraftlin commands to register
 * @see kraftlinCommand
 */
public fun Plugin.registerKraftlinCommands(vararg commands: KraftlinPaperCommand) {
    lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        val registrar = event.registrar()
        commands.forEach { command ->
            registrar.register(command.node, command.description, command.aliases)
        }
    }
}
