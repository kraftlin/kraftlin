@file:Suppress("UnstableApiUsage")

package io.github.kbrigx.paper.arguments

import io.github.kbrigx.core.argument
import io.github.kbrigx.paper.PaperArgumentNode
import io.github.kbrigx.paper.PaperContext
import io.github.kbrigx.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.position.ColumnBlockPosition
import io.papermc.paper.command.brigadier.argument.position.ColumnFinePosition
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnBlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnFinePositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver
import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.FinePosition

public fun PaperLiteralNode.blockPosition(
    name: String,
    block: PaperArgumentNode<BlockPositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.blockPosition(), block)

public fun PaperContext.blockPosition(name: String): BlockPosition =
    rawContext.getArgument(name, BlockPositionResolver::class.java).resolve(rawContext.source)


public fun PaperLiteralNode.columnBlockPosition(
    name: String,
    block: PaperArgumentNode<ColumnBlockPositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.columnBlockPosition(), block)

public fun PaperContext.columnBlockPosition(name: String): ColumnBlockPosition =
    rawContext.getArgument(name, ColumnBlockPositionResolver::class.java).resolve(rawContext.source)


public fun PaperLiteralNode.finePosition(
    name: String,
    block: PaperArgumentNode<FinePositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.finePosition(), block)

public fun PaperLiteralNode.finePosition(
    name: String,
    centerIntegers: Boolean,
    block: PaperArgumentNode<FinePositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.finePosition(centerIntegers), block)

public fun PaperContext.finePosition(name: String): FinePosition =
    rawContext.getArgument(name, FinePositionResolver::class.java).resolve(rawContext.source)


public fun PaperLiteralNode.columnFinePosition(
    name: String,
    block: PaperArgumentNode<ColumnFinePositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.columnFinePosition(), block)

public fun PaperLiteralNode.columnFinePosition(
    name: String,
    centerIntegers: Boolean,
    block: PaperArgumentNode<ColumnFinePositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.columnFinePosition(centerIntegers), block)

public fun PaperContext.columnFinePosition(name: String): ColumnFinePosition =
    rawContext.getArgument(name, ColumnFinePositionResolver::class.java).resolve(rawContext.source)
