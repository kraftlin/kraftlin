package io.github.kbrigx.paper

import com.mojang.brigadier.tree.LiteralCommandNode
import io.github.kbrigx.core.*
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

public typealias PaperSource = CommandSourceStack
public typealias PaperLiteralNode = LiteralNode<PaperSource>
public typealias PaperArgumentNode<T> = ArgumentNode<PaperSource, T>
public typealias PaperContext = KContext<PaperSource>
public typealias PaperExecuteScope = ExecuteScope<PaperSource>

public object PaperKBrigx {
    public fun command(
        name: String,
        block: PaperLiteralNode.() -> Unit,
    ): LiteralCommandNode<PaperSource> = KBrigx.command(name, block)
}

public fun PaperLiteralNode.executes(
    block: PaperExecuteScope.(CommandSender, PaperContext) -> Unit,
): Unit = executes { context ->
    this.block(context.sender, context)
}

public fun PaperLiteralNode.executesResult(
    block: PaperExecuteScope.(CommandSender, PaperContext) -> Int,
): Unit = executesResult { context ->
    this.block(context.sender, context)
}

public fun PaperLiteralNode.executesPlayer(
    block: PaperExecuteScope.(Player, PaperContext) -> Unit,
): Unit = executes { context -> this.block(context.requirePlayer(), context) }

public fun PaperLiteralNode.executesPlayerResult(
    block: PaperExecuteScope.(Player, PaperContext) -> Int,
): Unit = executesResult { context ->
    this.block(context.requirePlayer(), context)
}
