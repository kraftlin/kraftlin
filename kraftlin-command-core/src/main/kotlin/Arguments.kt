package io.github.kbrigx.core

import com.mojang.brigadier.arguments.*

public fun <S> LiteralNode<S>.word(
    name: String,
    block: ArgumentNode<S, String>.() -> Unit,
): Unit = argument(name, StringArgumentType.word(), block)

public fun <S> KContext<S>.word(name: String): String = string(name)


public fun <S> LiteralNode<S>.string(
    name: String,
    block: ArgumentNode<S, String>.() -> Unit,
): Unit = argument(name, StringArgumentType.string(), block)

public fun <S> LiteralNode<S>.greedyString(
    name: String,
    block: ArgumentNode<S, String>.() -> Unit,
): Unit = argument(name, StringArgumentType.greedyString(), block)

public fun <S> KContext<S>.greedyString(name: String): String = string(name)


public fun <S> LiteralNode<S>.integer(
    name: String,
    block: ArgumentNode<S, Int>.() -> Unit,
): Unit = argument(name, IntegerArgumentType.integer(), block)

public fun <S> LiteralNode<S>.integer(
    name: String,
    min: Int,
    block: ArgumentNode<S, Int>.() -> Unit,
): Unit = argument(name, IntegerArgumentType.integer(min), block)

public fun <S> LiteralNode<S>.integer(
    name: String,
    min: Int,
    max: Int,
    block: ArgumentNode<S, Int>.() -> Unit,
): Unit = argument(name, IntegerArgumentType.integer(min, max), block)

public fun <S> LiteralNode<S>.boolean(
    name: String,
    block: ArgumentNode<S, Boolean>.() -> Unit,
): Unit = argument(name, BoolArgumentType.bool(), block)

public fun <S> LiteralNode<S>.double(
    name: String,
    block: ArgumentNode<S, Double>.() -> Unit,
): Unit = argument(name, DoubleArgumentType.doubleArg(), block)

public fun <S> LiteralNode<S>.double(
    name: String,
    min: Double,
    block: ArgumentNode<S, Double>.() -> Unit,
): Unit = argument(name, DoubleArgumentType.doubleArg(min), block)

public fun <S> LiteralNode<S>.double(
    name: String,
    min: Double,
    max: Double,
    block: ArgumentNode<S, Double>.() -> Unit,
): Unit = argument(name, DoubleArgumentType.doubleArg(min, max), block)

public fun <S> LiteralNode<S>.long(
    name: String,
    block: ArgumentNode<S, Long>.() -> Unit,
): Unit = argument(name, LongArgumentType.longArg(), block)

public fun <S> LiteralNode<S>.long(
    name: String,
    min: Long,
    block: ArgumentNode<S, Long>.() -> Unit,
): Unit = argument(name, LongArgumentType.longArg(min), block)

public fun <S> LiteralNode<S>.long(
    name: String,
    min: Long,
    max: Long,
    block: ArgumentNode<S, Long>.() -> Unit,
): Unit = argument(name, LongArgumentType.longArg(min, max), block)

public fun <S> LiteralNode<S>.float(
    name: String,
    block: ArgumentNode<S, Float>.() -> Unit,
): Unit = argument(name, FloatArgumentType.floatArg(), block)

public fun <S> LiteralNode<S>.float(
    name: String,
    min: Float,
    block: ArgumentNode<S, Float>.() -> Unit,
): Unit = argument(name, FloatArgumentType.floatArg(min), block)

public fun <S> LiteralNode<S>.float(
    name: String,
    min: Float,
    max: Float,
    block: ArgumentNode<S, Float>.() -> Unit,
): Unit = argument(name, FloatArgumentType.floatArg(min, max), block)
