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

/**
 * Builds a Brigadier command tree rooted at a literal node named [name].
 *
 * This is the platform-agnostic entry point. Platform modules provide typed wrappers
 * (e.g. `kraftlinCommand` for Paper) that call this and handle registration.
 */
public fun <S> brigadierCommand(
    name: String,
    block: LiteralNode<S>.() -> Unit,
): LiteralCommandNode<S> {
    val root: LiteralArgumentBuilder<S> = LiteralArgumentBuilder.literal(name)
    val node = LiteralNode<S>(root)
    node.block()
    return root.build()
}

/** Marks Kraftlin command DSL scopes to prevent accidental nesting. */
@DslMarker
public annotation class CommandDsl

/**
 * Typed accessor for retrieving parsed argument values from a Brigadier [CommandContext].
 *
 * Passed to [executes] and [executesResult] blocks. Use the accessor methods
 * (e.g. [string], [integer]) to retrieve arguments by the name used when defining them.
 */
@CommandDsl
public class KContext<S>(
    public val rawContext: CommandContext<S>,
) {
    /** Returns the parsed [String] for the argument registered as [name]. */
    public fun string(name: String): String = StringArgumentType.getString(rawContext, name)
    /** Returns the parsed [Int] for the argument registered as [name]. */
    public fun integer(name: String): Int = IntegerArgumentType.getInteger(rawContext, name)
    /** Returns the parsed [Boolean] for the argument registered as [name]. */
    public fun boolean(name: String): Boolean = BoolArgumentType.getBool(rawContext, name)
    /** Returns the parsed [Double] for the argument registered as [name]. */
    public fun double(name: String): Double = DoubleArgumentType.getDouble(rawContext, name)
    /** Returns the parsed [Long] for the argument registered as [name]. */
    public fun long(name: String): Long = LongArgumentType.getLong(rawContext, name)
    /** Returns the parsed [Float] for the argument registered as [name]. */
    public fun float(name: String): Float = FloatArgumentType.getFloat(rawContext, name)

    /** The raw command source provided by the platform. */
    public val source: S get() = rawContext.source
}

/** Scope for building a command tree node. Provides [literal], [argument], [executes], and [requires]. */
@CommandDsl
public open class LiteralNode<S> internal constructor(
    public val builder: ArgumentBuilder<S, *>,
)

/** Scope for building a required argument node. Extends [LiteralNode] with [suggests]. */
@CommandDsl
public class ArgumentNode<S, T> internal constructor(
    public val argBuilder: RequiredArgumentBuilder<S, T>,
) : LiteralNode<S>(argBuilder)

/* -------------------------------------------------------------------------- */
/* Tree building                                                              */
/* -------------------------------------------------------------------------- */

/** Adds a literal sub-command named [name] with optional [aliases] that redirect to it. */
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

/** Adds a required argument of the given Brigadier [type]. Prefer the typed helpers (e.g. [string], [integer]). */
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

/** Adds a requirement that must be true for this node to be visible and executable. Stacks with previous requirements. */
public fun <S> LiteralNode<S>.requires(predicate: (S) -> Boolean): Unit = addRequirement(predicate)

/* -------------------------------------------------------------------------- */
/* Execution                                                                  */
/* -------------------------------------------------------------------------- */

/** Receiver for command execution blocks. Prevents nesting DSL scopes inside [executes]. */
@CommandDsl
public class ExecuteScope<S> internal constructor()

/** Registers the execution handler for this node. Returns `Command.SINGLE_SUCCESS` automatically. */
public fun <S> LiteralNode<S>.executes(
    block: ExecuteScope<S>.(KContext<S>) -> Unit,
) {
    executesResult { ctx ->
        block(ctx)
        Command.SINGLE_SUCCESS
    }
}

/** Registers an execution handler that returns a custom Brigadier result code. */
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

/** Registers a dynamic suggestion provider for this argument. */
public fun <S, T> ArgumentNode<S, T>.suggests(
    provider: (KContext<S>, SuggestionsBuilder) -> CompletableFuture<Suggestions>,
) {
    argBuilder.suggests { context, builder -> provider(KContext(context), builder) }
}

/** Registers a fixed set of suggestion [values] for this argument. */
public fun <S, T> ArgumentNode<S, T>.suggestsStatic(values: Iterable<String>) {
    suggests { _, b ->
        for (v in values) b.suggest(v)
        b.buildFuture()
    }
}

/** Registers a fixed set of suggestion [values] for this argument. */
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
    /** Extracts the platform-specific sender from the raw Brigadier source. */
    public fun sender(source: S): Sender
    /** Checks whether the source has the given [permission]. */
    public fun hasPermission(source: S, permission: String): Boolean
}

/** Execution handler that extracts the platform [Sender] before invoking [block]. */
public fun <S, Sender> LiteralNode<S>.executes(
    platform: PlatformAdapter<S, Sender>,
    block: ExecuteScope<S>.(Sender, KContext<S>) -> Unit,
): Unit = executes { context ->
    this.block(platform.sender(context.source), context)
}

/** Execution handler with custom result code that extracts the platform [Sender] before invoking [block]. */
public fun <S, Sender> LiteralNode<S>.executesResult(
    platform: PlatformAdapter<S, Sender>,
    block: ExecuteScope<S>.(Sender, KContext<S>) -> Int,
): Unit = executesResult { context ->
    this.block(platform.sender(context.source), context)
}

/** Requires the source to have the given [permission], checked via [platform]. */
public fun <S, Sender> LiteralNode<S>.requiresPermission(
    platform: PlatformAdapter<S, Sender>,
    permission: String,
): Unit = requires { platform.hasPermission(it, permission) }

/** Requires the platform sender to match a [predicate] (e.g. to check sender type). */
public fun <S, Sender> LiteralNode<S>.requiresSender(
    platform: PlatformAdapter<S, Sender>,
    predicate: (Sender) -> Boolean,
): Unit = requires { predicate(platform.sender(it)) }
