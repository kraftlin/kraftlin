package io.github.kraftlin.command.paper

import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
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
 * Set `overrideAliases = true` on a [KraftlinPaperCommand] to force alias registration to override
 * existing commands.
 *
 * @param commands one or more Kraftlin commands to register
 * @see kraftlinCommand
 */
public fun Plugin.registerKraftlinCommands(vararg commands: KraftlinPaperCommand) {
    lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        val registrar = event.registrar()
        commands.forEach { command ->
            if (command.overrideAliases) {
                registrar.register(command.node, command.description)
                for (alias in command.aliases) {
                    val aliasNode = cloneLiteralAs(command.node, alias)
                    registrar.register(aliasNode, command.description)
                }
            } else {
                registrar.register(command.node, command.description, command.aliases)
            }
        }
    }
}

private fun cloneLiteralAs(
    original: LiteralCommandNode<CommandSourceStack>,
    alias: String,
): LiteralCommandNode<CommandSourceStack> {
    val root = LiteralArgumentBuilder.literal<CommandSourceStack>(alias)
        .requires(original.requirement)
        .forward(original.redirect, original.redirectModifier, original.isFork)

    original.command?.let(root::executes)

    fun <S> clone(node: CommandNode<S>): ArgumentBuilder<S, *> =
        node.createBuilder().also { builder ->
            node.children.forEach { child -> builder.then(clone(child)) }
        }

    original.children.forEach { child -> root.then(clone(child)) }

    return root.build()
}
