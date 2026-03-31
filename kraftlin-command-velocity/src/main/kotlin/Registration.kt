package io.github.kraftlin.command.velocity

import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandManager


/**
 * Registers one or more Kraftlin commands with this command manager.
 *
 * This is intended to be called during plugin initialization.
 *
 * @param commands one or more Kraftlin commands to register
 * @see kraftlinCommand
 */
public fun CommandManager.registerKraftlinCommands(vararg commands: KraftlinVelocityCommand) {
    for (command in commands) {
        val brigadierCommand = BrigadierCommand(command.node)
        val meta = metaBuilder(brigadierCommand)
            .aliases(*command.aliases.toTypedArray())
            .build()
        register(meta, brigadierCommand)
    }
}
