@file:Suppress("UnstableApiUsage")

package io.github.kraftlin.command.paper.arguments

import io.github.kraftlin.command.argument
import io.github.kraftlin.command.paper.PaperArgumentNode
import io.github.kraftlin.command.paper.PaperContext
import io.github.kraftlin.command.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.predicate.BlockInWorldPredicate
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate
import org.bukkit.HeightMap
import org.bukkit.World
import org.bukkit.block.BlockState
import org.bukkit.inventory.ItemStack

/** Adds a [World] argument. @see [PaperContext.world] */
public fun PaperLiteralNode.world(
    name: String,
    block: PaperArgumentNode<World>.() -> Unit,
): Unit = argument(name, ArgumentTypes.world(), block)

/** Returns the parsed [World]. @see [PaperLiteralNode.world] */
public fun PaperContext.world(name: String): World = rawContext.getArgument<World>(name, World::class.java)


/** Adds a [BlockState] argument. @see [PaperContext.blockState] */
public fun PaperLiteralNode.blockState(
    name: String,
    block: PaperArgumentNode<BlockState>.() -> Unit,
): Unit = argument(name, ArgumentTypes.blockState(), block)

/** Returns the parsed [BlockState]. @see [PaperLiteralNode.blockState] */
public fun PaperContext.blockState(name: String): BlockState =
    rawContext.getArgument<BlockState>(name, BlockState::class.java)


/** Adds an [ItemStack] argument. @see [PaperContext.itemStack] */
public fun PaperLiteralNode.itemStack(
    name: String,
    block: PaperArgumentNode<ItemStack>.() -> Unit,
): Unit = argument(name, ArgumentTypes.itemStack(), block)

/** Returns the parsed [ItemStack]. @see [PaperLiteralNode.itemStack] */
public fun PaperContext.itemStack(name: String): ItemStack =
    rawContext.getArgument<ItemStack>(name, ItemStack::class.java)


/** Adds a [BlockInWorldPredicate] argument for testing blocks in the world. @see [PaperContext.blockInWorldPredicate] */
public fun PaperLiteralNode.blockInWorldPredicate(
    name: String,
    block: PaperArgumentNode<BlockInWorldPredicate>.() -> Unit,
): Unit = argument(name, ArgumentTypes.blockInWorldPredicate(), block)

/** Returns the parsed [BlockInWorldPredicate]. @see [PaperLiteralNode.blockInWorldPredicate] */
public fun PaperContext.blockInWorldPredicate(name: String): BlockInWorldPredicate =
    rawContext.getArgument<BlockInWorldPredicate>(name, BlockInWorldPredicate::class.java)


/** Adds an [ItemStackPredicate] argument for testing item stacks. @see [PaperContext.itemPredicate] */
public fun PaperLiteralNode.itemPredicate(
    name: String,
    block: PaperArgumentNode<ItemStackPredicate>.() -> Unit,
): Unit = argument(name, ArgumentTypes.itemPredicate(), block)

/** Returns the parsed [ItemStackPredicate]. @see [PaperLiteralNode.itemPredicate] */
public fun PaperContext.itemPredicate(name: String): ItemStackPredicate =
    rawContext.getArgument<ItemStackPredicate>(name, ItemStackPredicate::class.java)


/** Adds a [HeightMap] argument. @see [PaperContext.heightMap] */
public fun PaperLiteralNode.heightMap(
    name: String,
    block: PaperArgumentNode<HeightMap>.() -> Unit,
): Unit = argument(name, ArgumentTypes.heightMap(), block)

/** Returns the parsed [HeightMap]. @see [PaperLiteralNode.heightMap] */
public fun PaperContext.heightMap(name: String): HeightMap =
    rawContext.getArgument<HeightMap>(name, HeightMap::class.java)


/** Adds a time argument (in ticks). @see [PaperContext.time] */
public fun PaperLiteralNode.time(
    name: String,
    block: PaperArgumentNode<Int>.() -> Unit,
): Unit = argument(name, ArgumentTypes.time(), block)

/** Adds a time argument (in ticks) with minimum [minTime]. @see [PaperContext.time] */
public fun PaperLiteralNode.time(
    name: String,
    minTime: Int,
    block: PaperArgumentNode<Int>.() -> Unit,
): Unit = argument(name, ArgumentTypes.time(minTime), block)

/** Returns the parsed time value in ticks. @see [PaperLiteralNode.time] */
public fun PaperContext.time(name: String): Int = integer(name)
