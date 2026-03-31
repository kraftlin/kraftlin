package io.github.kraftlin.command

import com.mojang.brigadier.arguments.*

/** Adds a single-word string argument. @see [KContext.word] */
public fun <S> LiteralNode<S>.word(
    name: String,
    block: ArgumentNode<S, String>.() -> Unit,
): Unit = argument(name, StringArgumentType.word(), block)

/** Returns the string value. Alias for [string]. @see [LiteralNode.word] */
public fun <S> KContext<S>.word(name: String): String = string(name)


/** Adds a quoted or single-word string argument. */
public fun <S> LiteralNode<S>.string(
    name: String,
    block: ArgumentNode<S, String>.() -> Unit,
): Unit = argument(name, StringArgumentType.string(), block)

/** Adds a greedy string argument that consumes all remaining input. @see [KContext.greedyString] */
public fun <S> LiteralNode<S>.greedyString(
    name: String,
    block: ArgumentNode<S, String>.() -> Unit,
): Unit = argument(name, StringArgumentType.greedyString(), block)

/** Returns the string value. Alias for [string]. @see [LiteralNode.greedyString] */
public fun <S> KContext<S>.greedyString(name: String): String = string(name)


/** Adds an integer argument. */
public fun <S> LiteralNode<S>.integer(
    name: String,
    block: ArgumentNode<S, Int>.() -> Unit,
): Unit = argument(name, IntegerArgumentType.integer(), block)

/** Adds an integer argument with minimum [min]. */
public fun <S> LiteralNode<S>.integer(
    name: String,
    min: Int,
    block: ArgumentNode<S, Int>.() -> Unit,
): Unit = argument(name, IntegerArgumentType.integer(min), block)

/** Adds an integer argument bounded to [min]..[max]. */
public fun <S> LiteralNode<S>.integer(
    name: String,
    min: Int,
    max: Int,
    block: ArgumentNode<S, Int>.() -> Unit,
): Unit = argument(name, IntegerArgumentType.integer(min, max), block)

/** Adds a boolean argument. */
public fun <S> LiteralNode<S>.boolean(
    name: String,
    block: ArgumentNode<S, Boolean>.() -> Unit,
): Unit = argument(name, BoolArgumentType.bool(), block)

/** Adds a double argument. */
public fun <S> LiteralNode<S>.double(
    name: String,
    block: ArgumentNode<S, Double>.() -> Unit,
): Unit = argument(name, DoubleArgumentType.doubleArg(), block)

/** Adds a double argument with minimum [min]. */
public fun <S> LiteralNode<S>.double(
    name: String,
    min: Double,
    block: ArgumentNode<S, Double>.() -> Unit,
): Unit = argument(name, DoubleArgumentType.doubleArg(min), block)

/** Adds a double argument bounded to [min]..[max]. */
public fun <S> LiteralNode<S>.double(
    name: String,
    min: Double,
    max: Double,
    block: ArgumentNode<S, Double>.() -> Unit,
): Unit = argument(name, DoubleArgumentType.doubleArg(min, max), block)

/** Adds a long argument. */
public fun <S> LiteralNode<S>.long(
    name: String,
    block: ArgumentNode<S, Long>.() -> Unit,
): Unit = argument(name, LongArgumentType.longArg(), block)

/** Adds a long argument with minimum [min]. */
public fun <S> LiteralNode<S>.long(
    name: String,
    min: Long,
    block: ArgumentNode<S, Long>.() -> Unit,
): Unit = argument(name, LongArgumentType.longArg(min), block)

/** Adds a long argument bounded to [min]..[max]. */
public fun <S> LiteralNode<S>.long(
    name: String,
    min: Long,
    max: Long,
    block: ArgumentNode<S, Long>.() -> Unit,
): Unit = argument(name, LongArgumentType.longArg(min, max), block)

/** Adds a float argument. */
public fun <S> LiteralNode<S>.float(
    name: String,
    block: ArgumentNode<S, Float>.() -> Unit,
): Unit = argument(name, FloatArgumentType.floatArg(), block)

/** Adds a float argument with minimum [min]. */
public fun <S> LiteralNode<S>.float(
    name: String,
    min: Float,
    block: ArgumentNode<S, Float>.() -> Unit,
): Unit = argument(name, FloatArgumentType.floatArg(min), block)

/** Adds a float argument bounded to [min]..[max]. */
public fun <S> LiteralNode<S>.float(
    name: String,
    min: Float,
    max: Float,
    block: ArgumentNode<S, Float>.() -> Unit,
): Unit = argument(name, FloatArgumentType.floatArg(min, max), block)
