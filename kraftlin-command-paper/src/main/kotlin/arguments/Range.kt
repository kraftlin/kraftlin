package io.github.kbrigx.paper.arguments

import com.google.common.collect.Range
import io.github.kbrigx.core.argument
import io.github.kbrigx.paper.PaperArgumentNode
import io.github.kbrigx.paper.PaperContext
import io.github.kbrigx.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.range.DoubleRangeProvider
import io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider

public fun PaperLiteralNode.integerRange(
    name: String,
    block: PaperArgumentNode<IntegerRangeProvider>.() -> Unit,
): Unit = argument(name, ArgumentTypes.integerRange(), block)

public fun PaperContext.integerRange(name: String): Range<Int> =
    rawContext.getArgument(name, IntegerRangeProvider::class.java).range()


public fun PaperLiteralNode.doubleRange(
    name: String,
    block: PaperArgumentNode<DoubleRangeProvider>.() -> Unit,
): Unit = argument(name, ArgumentTypes.doubleRange(), block)

public fun PaperContext.doubleRange(name: String): Range<Double> =
    rawContext.getArgument(name, DoubleRangeProvider::class.java).range()
