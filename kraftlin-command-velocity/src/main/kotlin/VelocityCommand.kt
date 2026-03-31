package io.github.kraftlin.command.velocity

import com.mojang.brigadier.tree.LiteralCommandNode
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import io.github.kraftlin.command.*

public typealias VelocitySource = CommandSource
public typealias VelocityLiteralNode = LiteralNode<VelocitySource>
public typealias VelocityArgumentNode<T> = ArgumentNode<VelocitySource, T>
public typealias VelocityContext = KContext<VelocitySource>
public typealias VelocityExecuteScope = ExecuteScope<VelocitySource>


public data class KraftlinVelocityCommand(
    public val node: LiteralCommandNode<VelocitySource>,
    public val description: String?,
    public val aliases: List<String>,
)


/**
 * Declares a new Kraftlin command for Velocity.
 *
 * This is the main entry point for defining commands using the Kraftlin DSL.
 * It creates the root literal node, applies the DSL block, and returns a
 * [KraftlinVelocityCommand] that can be registered via [registerKraftlinCommands].
 *
 * Example:
 * ```
 * val demo = kraftlinCommand(
 *     name = "demo",
 *     description = "Demo command",
 *     aliases = listOf("d")
 * ) {
 *     executes { sender, context ->
 *         sender.sendMessage(Component.text("Hello from Velocity!"))
 *     }
 * }
 * ```
 *
 * @param name the root command literal (e.g. "demo")
 * @param description optional description shown in help output
 * @param aliases optional alternative labels for the command
 * @param block DSL block used to build the command tree
 * @return a [KraftlinVelocityCommand] that can be registered via [registerKraftlinCommands]
 * @see registerKraftlinCommands
 */
public fun kraftlinCommand(
    name: String,
    description: String? = null,
    aliases: List<String> = emptyList(),
    block: VelocityLiteralNode.() -> Unit,
): KraftlinVelocityCommand {
    return KraftlinVelocityCommand(
        node = brigadierCommand(name, block),
        description = description,
        aliases = aliases,
    )
}

public fun VelocityLiteralNode.executes(
    block: VelocityExecuteScope.(CommandSource, VelocityContext) -> Unit,
): Unit = executes { context ->
    this.block(context.sender, context)
}

public fun VelocityLiteralNode.executesResult(
    block: VelocityExecuteScope.(CommandSource, VelocityContext) -> Int,
): Unit = executesResult { context ->
    this.block(context.sender, context)
}

public fun VelocityLiteralNode.executesPlayer(
    block: VelocityExecuteScope.(Player, VelocityContext) -> Unit,
): Unit = executes { context -> this.block(context.requirePlayer(), context) }

public fun VelocityLiteralNode.executesPlayerResult(
    block: VelocityExecuteScope.(Player, VelocityContext) -> Int,
): Unit = executesResult { context ->
    this.block(context.requirePlayer(), context)
}
