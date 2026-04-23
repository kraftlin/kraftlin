package io.github.kraftlin.command.paper.arguments

import io.github.kraftlin.command.argument
import io.github.kraftlin.command.paper.PaperArgumentNode
import io.github.kraftlin.command.paper.PaperContext
import io.github.kraftlin.command.paper.PaperLiteralNode
import io.github.kraftlin.command.paper.arguments.internal.enumChoice
import io.github.kraftlin.command.paper.arguments.internal.stringChoice


/**
 * Adds a string argument that must match one of the provided [values].
 *
 * Values must not contain whitespace — each value is parsed as a single word.
 * If you need values with spaces, use [string] (accepts quoted strings) or
 * [greedyString] (consumes remaining input) and validate in the executor.
 *
 * Tab-completion suggests [values] in the order given. Invalid input is
 * rejected on the server at execution time and fed back to the client as a command parsing error.
 *
 * Implementation: wraps Paper's `CustomArgumentType.Converted` over
 * Brigadier's `StringArgumentType.word()`. See
 * [Custom arguments](https://docs.papermc.io/paper/dev/command-api/basics/custom-arguments/).
 *
 * @see PaperContext.choice for retrieving the value
 * @see string for quoted strings with spaces
 * @see greedyString for unconstrained trailing input
 */
public fun PaperLiteralNode.choice(
    name: String,
    values: Iterable<String>,
    block: PaperArgumentNode<String>.() -> Unit,
): Unit = argument(name, stringChoice(values), block)

/**
 * Returns the parsed value of a [choice] argument.
 *
 * This will return the exact string token selected by the user. The value is guaranteed to be one
 * of the allowed choices if the command handler is invoked.
 *
 * @see PaperLiteralNode.choice
 */
public fun PaperContext.choice(name: String): String = rawContext.getArgument(name, String::class.java)


/**
 * Adds an enum-backed "choice" argument to this command.
 *
 * Each enum constant is mapped to a lowercase token by default (e.g. `FOO_BAR` -> `foo_bar`).
 * The argument will only accept those tokens and will suggest them for tab-completion.
 *
 * Implementation detail:
 * This uses Paper's `CustomArgumentType.Converted`, delegating to a native
 * `StringArgumentType.word()` on the client and converting the string to the enum on the server.
 *
 * Consequences:
 * - Client-side syntax validation only sees a generic string argument.
 * - Invalid enum values are rejected on the server when the command is executed.
 * - Suggestions are provided by the server in enum declaration order.
 *
 * Documentation: [Custom arguments](https://docs.papermc.io/paper/dev/command-api/basics/custom-arguments/)
 * @see PaperContext.enum
 */
public inline fun <reified E : Enum<E>> PaperLiteralNode.enum(
    name: String,
    values: Iterable<E> = enumValues<E>().asList(),
    noinline block: PaperArgumentNode<E>.() -> Unit,
): Unit = argument(name, enumChoice(values), block)

/**
 * Returns the parsed enum value of an [enum] argument.
 *
 * @see PaperLiteralNode.enum
 */
public inline fun <reified E : Enum<E>> PaperContext.enum(name: String): E = rawContext.getArgument(name, E::class.java)
