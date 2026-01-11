package io.github.kraftlin.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import java.util.concurrent.CompletableFuture

public fun <S> brigadierCommand(
    name: String,
    block: LiteralNode<S>.() -> Unit,
): LiteralCommandNode<S> {
    val root: LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal(name)
    val node = LiteralNode<S>(root)
    node.block()
    return root.build()
}

@CommandDsl
public class KContext<S>(
    public val rawContext: CommandContext<S>,
) {
    public fun string(name: String): String = StringArgumentType.getString(rawContext, name)
    public fun integer(name: String): Int = IntegerArgumentType.getInteger(rawContext, name)
    public fun boolean(name: String): Boolean = BoolArgumentType.getBool(rawContext, name)
    public fun double(name: String): Double = DoubleArgumentType.getDouble(rawContext, name)
    public fun long(name: String): Long = LongArgumentType.getLong(rawContext, name)
    public fun float(name: String): Float = FloatArgumentType.getFloat(rawContext, name)

    public val source: S get() = rawContext.source
}

@CommandDsl
public open class LiteralNode<S> internal constructor(
    public val builder: ArgumentBuilder<S, *>,
)

@CommandDsl
public class ArgumentNode<S, T> internal constructor(
    public val argBuilder: RequiredArgumentBuilder<S, T>,
) : LiteralNode<S>(argBuilder)

/* -------------------------------------------------------------------------- */
/* Tree building                                                              */
/* -------------------------------------------------------------------------- */

public fun <S> LiteralNode<S>.literal(
    name: String,
    block: LiteralNode<S>.() -> Unit,
) {
    val child: LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal(name)
    val node = LiteralNode<S>(child)
    node.block()
    builder.then(child)
}

public fun <S, T> LiteralNode<S>.argument(
    name: String,
    type: ArgumentType<T>,
    block: ArgumentNode<S, T>.() -> Unit,
) {
    val arg: RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder.argument(name, type)
    val node = ArgumentNode(arg)
    node.block()
    builder.then(arg)
}

/* -------------------------------------------------------------------------- */
/* Requirements                                                               */
/* -------------------------------------------------------------------------- */

private fun <S> LiteralNode<S>.addRequirement(extra: (S) -> Boolean) {
    val previous = builder.requirement
    builder.requires { s: S -> previous.test(s) && extra(s) }
}

public fun <S> LiteralNode<S>.requires(predicate: (S) -> Boolean): Unit = addRequirement(predicate)

/* -------------------------------------------------------------------------- */
/* Execution                                                                  */
/* -------------------------------------------------------------------------- */

@CommandDsl
public class ExecuteScope<S> internal constructor()

public fun <S> LiteralNode<S>.executes(
    block: ExecuteScope<S>.(KContext<S>) -> Unit,
) {
    executesResult { ctx ->
        block(ctx)
        Command.SINGLE_SUCCESS
    }
}

public fun <S> LiteralNode<S>.executesResult(
    block: ExecuteScope<S>.(KContext<S>) -> Int,
) {
    builder.executes { context ->
        ExecuteScope<S>().block(KContext(context))
    }
}

/* -------------------------------------------------------------------------- */
/* Suggestions                                                                */
/* -------------------------------------------------------------------------- */

public fun <S, T> ArgumentNode<S, T>.suggests(
    provider: (KContext<S>, SuggestionsBuilder) -> CompletableFuture<Suggestions>,
) {
    argBuilder.suggests { context, builder -> provider(KContext(context), builder) }
}

public fun <S, T> ArgumentNode<S, T>.suggestsStatic(values: Iterable<String>) {
    suggests { _, b ->
        for (v in values) b.suggest(v)
        b.buildFuture()
    }
}

public fun <S, T> ArgumentNode<S, T>.suggestsStatic(vararg values: String): Unit = suggestsStatic(values.asList())
