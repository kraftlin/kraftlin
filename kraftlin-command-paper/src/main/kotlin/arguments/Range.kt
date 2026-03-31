package io.github.kraftlin.command.paper.arguments

import com.google.common.collect.Range
import io.github.kraftlin.command.argument
import io.github.kraftlin.command.paper.PaperArgumentNode
import io.github.kraftlin.command.paper.PaperContext
import io.github.kraftlin.command.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.range.DoubleRangeProvider
import io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider

/** Adds an integer range argument (e.g. `1..5`). @see [PaperContext.integerRange] */
public fun PaperLiteralNode.integerRange(
    name: String,
    block: PaperArgumentNode<IntegerRangeProvider>.() -> Unit,
): Unit = argument(name, ArgumentTypes.integerRange(), block)

/** Returns the parsed integer [Range]. @see [PaperLiteralNode.integerRange] */
public fun PaperContext.integerRange(name: String): Range<Int> =
    rawContext.getArgument(name, IntegerRangeProvider::class.java).range()


/** Adds a double range argument (e.g. `0.5..1.0`). @see [PaperContext.doubleRange] */
public fun PaperLiteralNode.doubleRange(
    name: String,
    block: PaperArgumentNode<DoubleRangeProvider>.() -> Unit,
): Unit = argument(name, ArgumentTypes.doubleRange(), block)

/** Returns the parsed double [Range]. @see [PaperLiteralNode.doubleRange] */
public fun PaperContext.doubleRange(name: String): Range<Double> =
    rawContext.getArgument(name, DoubleRangeProvider::class.java).range()
