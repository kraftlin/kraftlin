@file:Suppress("UnstableApiUsage")

package io.github.kraftlin.command.paper.arguments

import io.github.kraftlin.command.argument
import io.github.kraftlin.command.paper.PaperArgumentNode
import io.github.kraftlin.command.paper.PaperContext
import io.github.kraftlin.command.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.position.ColumnBlockPosition
import io.papermc.paper.command.brigadier.argument.position.ColumnFinePosition
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnBlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnFinePositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver
import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.FinePosition

/** Adds a block position (x, y, z integers) argument. @see [PaperContext.blockPosition] */
public fun PaperLiteralNode.blockPosition(
    name: String,
    block: PaperArgumentNode<BlockPositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.blockPosition(), block)

/** Returns the resolved [BlockPosition]. @see [PaperLiteralNode.blockPosition] */
public fun PaperContext.blockPosition(name: String): BlockPosition =
    rawContext.getArgument(name, BlockPositionResolver::class.java).resolve(rawContext.source)


/** Adds a column block position (x, z integers) argument. @see [PaperContext.columnBlockPosition] */
public fun PaperLiteralNode.columnBlockPosition(
    name: String,
    block: PaperArgumentNode<ColumnBlockPositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.columnBlockPosition(), block)

/** Returns the resolved [ColumnBlockPosition]. @see [PaperLiteralNode.columnBlockPosition] */
public fun PaperContext.columnBlockPosition(name: String): ColumnBlockPosition =
    rawContext.getArgument(name, ColumnBlockPositionResolver::class.java).resolve(rawContext.source)


/** Adds a fine position (x, y, z doubles) argument. @see [PaperContext.finePosition] */
public fun PaperLiteralNode.finePosition(
    name: String,
    block: PaperArgumentNode<FinePositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.finePosition(), block)

/** Adds a fine position argument. When [centerIntegers] is true, integer inputs are centered (+0.5). @see [PaperContext.finePosition] */
public fun PaperLiteralNode.finePosition(
    name: String,
    centerIntegers: Boolean,
    block: PaperArgumentNode<FinePositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.finePosition(centerIntegers), block)

/** Returns the resolved [FinePosition]. @see [PaperLiteralNode.finePosition] */
public fun PaperContext.finePosition(name: String): FinePosition =
    rawContext.getArgument(name, FinePositionResolver::class.java).resolve(rawContext.source)


/** Adds a column fine position (x, z doubles) argument. @see [PaperContext.columnFinePosition] */
public fun PaperLiteralNode.columnFinePosition(
    name: String,
    block: PaperArgumentNode<ColumnFinePositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.columnFinePosition(), block)

/** Adds a column fine position argument. When [centerIntegers] is true, integer inputs are centered (+0.5). @see [PaperContext.columnFinePosition] */
public fun PaperLiteralNode.columnFinePosition(
    name: String,
    centerIntegers: Boolean,
    block: PaperArgumentNode<ColumnFinePositionResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.columnFinePosition(centerIntegers), block)

/** Returns the resolved [ColumnFinePosition]. @see [PaperLiteralNode.columnFinePosition] */
public fun PaperContext.columnFinePosition(name: String): ColumnFinePosition =
    rawContext.getArgument(name, ColumnFinePositionResolver::class.java).resolve(rawContext.source)
