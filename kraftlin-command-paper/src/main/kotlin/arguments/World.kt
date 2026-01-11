@file:Suppress("UnstableApiUsage")

package io.github.kbrigx.paper.arguments

import io.github.kbrigx.core.argument
import io.github.kbrigx.paper.PaperArgumentNode
import io.github.kbrigx.paper.PaperContext
import io.github.kbrigx.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.predicate.BlockInWorldPredicate
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate
import org.bukkit.HeightMap
import org.bukkit.World
import org.bukkit.block.BlockState
import org.bukkit.inventory.ItemStack

public fun PaperLiteralNode.world(
    name: String,
    block: PaperArgumentNode<World>.() -> Unit,
): Unit = argument(name, ArgumentTypes.world(), block)

public fun PaperContext.world(name: String): World = rawContext.getArgument<World>(name, World::class.java)


public fun PaperLiteralNode.blockState(
    name: String,
    block: PaperArgumentNode<BlockState>.() -> Unit,
): Unit = argument(name, ArgumentTypes.blockState(), block)

public fun PaperContext.blockState(name: String): BlockState =
    rawContext.getArgument<BlockState>(name, BlockState::class.java)


public fun PaperLiteralNode.itemStack(
    name: String,
    block: PaperArgumentNode<ItemStack>.() -> Unit,
): Unit = argument(name, ArgumentTypes.itemStack(), block)

public fun PaperContext.itemStack(name: String): ItemStack =
    rawContext.getArgument<ItemStack>(name, ItemStack::class.java)


public fun PaperLiteralNode.blockInWorldPredicate(
    name: String,
    block: PaperArgumentNode<BlockInWorldPredicate>.() -> Unit,
): Unit = argument(name, ArgumentTypes.blockInWorldPredicate(), block)

public fun PaperContext.blockInWorldPredicate(name: String): BlockInWorldPredicate =
    rawContext.getArgument<BlockInWorldPredicate>(name, BlockInWorldPredicate::class.java)


public fun PaperLiteralNode.itemPredicate(
    name: String,
    block: PaperArgumentNode<ItemStackPredicate>.() -> Unit,
): Unit = argument(name, ArgumentTypes.itemPredicate(), block)

public fun PaperContext.itemPredicate(name: String): ItemStackPredicate =
    rawContext.getArgument<ItemStackPredicate>(name, ItemStackPredicate::class.java)


public fun PaperLiteralNode.heightMap(
    name: String,
    block: PaperArgumentNode<HeightMap>.() -> Unit,
): Unit = argument(name, ArgumentTypes.heightMap(), block)

public fun PaperContext.heightMap(name: String): HeightMap =
    rawContext.getArgument<HeightMap>(name, HeightMap::class.java)


public fun PaperLiteralNode.time(
    name: String,
    block: PaperArgumentNode<Int>.() -> Unit,
): Unit = argument(name, ArgumentTypes.time(), block)

public fun PaperLiteralNode.time(
    name: String,
    minTime: Int,
    block: PaperArgumentNode<Int>.() -> Unit,
): Unit = argument(name, ArgumentTypes.time(minTime), block)

public fun PaperContext.time(name: String): Int = integer(name)
