package io.github.kbrigx.paper

import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

public fun JavaPlugin.registerBrigxCommands(
    block: BrigxRegistrationScope.() -> Unit
) {
    lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
        val scope = BrigxRegistrationScope(event.registrar())
        scope.block()
    }
}

public class BrigxRegistrationScope internal constructor(
    private val commands: Commands
) {

    public fun command(
        node: LiteralCommandNode<CommandSourceStack>,
        description: String? = null,
        aliases: Collection<String> = emptyList(),
    ) {
        when {
            description != null && aliases.isNotEmpty() ->
                commands.register(node, description, aliases)

            description != null ->
                commands.register(node, description)

            aliases.isNotEmpty() ->
                commands.register(node, aliases)

            else ->
                commands.register(node)
        }
    }
}
