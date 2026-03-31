package io.github.kraftlin.command.paper

import com.mojang.brigadier.tree.LiteralCommandNode
import io.github.kraftlin.command.*
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/** Paper's Brigadier command source type. */
public typealias PaperSource = CommandSourceStack
/** [LiteralNode] specialized to Paper's [CommandSourceStack]. */
public typealias PaperLiteralNode = LiteralNode<PaperSource>
/** [ArgumentNode] specialized to Paper's [CommandSourceStack]. */
public typealias PaperArgumentNode<T> = ArgumentNode<PaperSource, T>
/** [KContext] specialized to Paper's [CommandSourceStack]. */
public typealias PaperContext = KContext<PaperSource>
/** [ExecuteScope] specialized to Paper's [CommandSourceStack]. */
public typealias PaperExecuteScope = ExecuteScope<PaperSource>


/**
 * A built command ready for registration via [registerKraftlinCommands].
 *
 * @property node the root Brigadier command node
 * @property description optional description shown in help output
 * @property aliases alternative labels for the command
 * @property overrideAliases whether aliases should override existing commands from vanilla or other plugins
 */
public data class KraftlinPaperCommand(
    public val node: LiteralCommandNode<PaperSource>,
    public val description: String?,
    public val aliases: List<String>,
    public val overrideAliases: Boolean,
)


/**
 * Declares a new Kraftlin command for Paper.
 *
 * This is the main entry point for defining commands using the Kraftlin DSL.
 * It creates the root literal node, applies the DSL block, and returns a
 * [KraftlinPaperCommand] that can be registered via [registerKraftlinCommands].
 *
 * Example:
 * ```
 * val demo = kraftlinCommand(
 *     name = "demo",
 *     description = "Demo command",
 *     aliases = listOf("d")
 * ) {
 *     player("target") {
 *         executes { sender, context ->
 *             val target = context.player("target")
 *             target.sendMessage("Hello")
 *             sender.sendMessage("Sent hello to ${target.name}")
 *         }
 *     }
 * }
 * ```
 *
 * @param name the root command literal (e.g. "demo")
 * @param description optional description shown in help output
 * @param aliases optional alternative labels for the command
 * @param block DSL block used to build the command tree
 * @return a [KraftlinPaperCommand] that can be registered via [registerKraftlinCommands]
 * @see registerKraftlinCommands
 */
public fun kraftlinCommand(
    name: String,
    description: String? = null,
    aliases: List<String> = emptyList(),
    overrideAliases: Boolean = false,
    block: PaperLiteralNode.() -> Unit,
): KraftlinPaperCommand {
    return KraftlinPaperCommand(
        node = brigadierCommand(name, block),
        description = description,
        aliases = aliases,
        overrideAliases = overrideAliases,
    )
}

/** Execution handler that extracts the [CommandSender] before invoking [block]. */
public fun PaperLiteralNode.executes(
    block: PaperExecuteScope.(CommandSender, PaperContext) -> Unit,
): Unit = executes { context ->
    this.block(context.sender, context)
}

/** Execution handler with custom result code that extracts the [CommandSender]. */
public fun PaperLiteralNode.executesResult(
    block: PaperExecuteScope.(CommandSender, PaperContext) -> Int,
): Unit = executesResult { context ->
    this.block(context.sender, context)
}

/** Execution handler restricted to [Player] senders. Throws a command error if the sender is not a player. */
public fun PaperLiteralNode.executesPlayer(
    block: PaperExecuteScope.(Player, PaperContext) -> Unit,
): Unit = executes { context -> this.block(context.requirePlayer(), context) }

/** Execution handler with custom result code, restricted to [Player] senders. */
public fun PaperLiteralNode.executesPlayerResult(
    block: PaperExecuteScope.(Player, PaperContext) -> Int,
): Unit = executesResult { context ->
    this.block(context.requirePlayer(), context)
}
