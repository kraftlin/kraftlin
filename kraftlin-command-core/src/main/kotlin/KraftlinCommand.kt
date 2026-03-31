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

@DslMarker
public annotation class CommandDsl

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
    vararg aliases: String,
    block: LiteralNode<S>.() -> Unit,
) {
    // Build the primary command node
    val primaryBuilder = LiteralArgumentBuilder.literal<S>(name)
    val primaryNodeBuilder = LiteralNode(primaryBuilder)
    primaryNodeBuilder.block()

    val primaryNode = primaryBuilder.build()
    builder.then(primaryNode)

    // Add alias literals as siblings that redirect to the primary node
    for (alias in aliases) {
        builder.then(LiteralArgumentBuilder.literal<S>(alias).redirect(primaryNode))
    }
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

/* -------------------------------------------------------------------------- */
/* Platform adapter                                                           */
/* -------------------------------------------------------------------------- */

/**
 * Abstraction over platform-specific command source handling.
 *
 * Each platform (Paper, Velocity, …) provides an implementation that bridges between
 * Brigadier's raw source type [S] and the platform's sender/audience type [Sender].
 * This allows command definitions to be written once and used across platforms.
 *
 * @param S the Brigadier command source type (e.g. `CommandSourceStack`, `CommandSource`)
 * @param Sender the platform's sender type (e.g. `CommandSender`, `CommandSource`)
 */
public interface PlatformAdapter<S, Sender> {
    public fun sender(source: S): Sender
    public fun hasPermission(source: S, permission: String): Boolean
}

public fun <S, Sender> LiteralNode<S>.executes(
    platform: PlatformAdapter<S, Sender>,
    block: ExecuteScope<S>.(Sender, KContext<S>) -> Unit,
): Unit = executes { context ->
    this.block(platform.sender(context.source), context)
}

public fun <S, Sender> LiteralNode<S>.executesResult(
    platform: PlatformAdapter<S, Sender>,
    block: ExecuteScope<S>.(Sender, KContext<S>) -> Int,
): Unit = executesResult { context ->
    this.block(platform.sender(context.source), context)
}

public fun <S, Sender> LiteralNode<S>.requiresPermission(
    platform: PlatformAdapter<S, Sender>,
    permission: String,
): Unit = requires { platform.hasPermission(it, permission) }

public fun <S, Sender> LiteralNode<S>.requiresSender(
    platform: PlatformAdapter<S, Sender>,
    predicate: (Sender) -> Boolean,
): Unit = requires { predicate(platform.sender(it)) }
