package io.github.kraftlin.command.velocity.arguments

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import io.github.kraftlin.command.argument
import io.github.kraftlin.command.suggestsStatic
import io.github.kraftlin.command.velocity.VelocityArgumentNode
import io.github.kraftlin.command.velocity.VelocityContext
import io.github.kraftlin.command.velocity.VelocityLiteralNode


/**
 * Adds a string "choice" argument to this command.
 *
 * The argument only accepts one of the provided [values] and will suggest them for tab-completion.
 *
 * Since Velocity has no custom argument type adapter, this uses a native `StringArgumentType.word()`
 * with server-side suggestions. The client sees a generic string argument. Invalid values are
 * rejected when [VelocityContext.choice] is called during command execution.
 *
 * @see VelocityContext.choice
 */
public fun VelocityLiteralNode.choice(
    name: String,
    values: Iterable<String>,
    block: VelocityArgumentNode<String>.() -> Unit,
): Unit = argument(name, StringArgumentType.word()) {
    suggestsStatic(values.toList())
    block()
}

/**
 * Returns the parsed value of a [choice] argument.
 *
 * @param allowed the set of valid choices; must match the values passed to [choice] at registration.
 * @throws com.mojang.brigadier.exceptions.CommandSyntaxException if the input is not in [allowed]
 * @see VelocityLiteralNode.choice
 */
public fun VelocityContext.choice(name: String, allowed: Collection<String>): String {
    val input = string(name)
    if (input in allowed) return input
    throw SimpleCommandExceptionType(
        LiteralMessage("Invalid value '$input'. Expected one of: ${allowed.joinToString(", ")}")
    ).create()
}

/**
 * Adds an enum-backed "choice" argument to this command.
 *
 * Each enum constant is mapped to a lowercase token (e.g. `FOO_BAR` -> `foo_bar`).
 * The argument will suggest those tokens for tab-completion.
 *
 * Since Velocity has no custom argument type adapter, this uses a native `StringArgumentType.word()`
 * with server-side suggestions. Invalid values are rejected when [VelocityContext.enum] is called
 * during command execution.
 *
 * @see VelocityContext.enum
 */
public inline fun <reified E : Enum<E>> VelocityLiteralNode.enum(
    name: String,
    values: Iterable<E> = enumValues<E>().asList(),
    noinline block: VelocityArgumentNode<String>.() -> Unit,
): Unit = argument(name, StringArgumentType.word()) {
    suggestsStatic(values.map { it.name.lowercase() })
    block()
}

/**
 * Returns the parsed enum value of an [enum] argument.
 *
 * @throws com.mojang.brigadier.exceptions.CommandSyntaxException if the input does not match any enum constant
 * @see VelocityLiteralNode.enum
 */
public inline fun <reified E : Enum<E>> VelocityContext.enum(name: String): E {
    val input = string(name)
    val value = enumValues<E>().firstOrNull { it.name.lowercase() == input }
    if (value != null) return value
    val allowed = enumValues<E>().joinToString(", ") { it.name.lowercase() }
    throw SimpleCommandExceptionType(
        LiteralMessage("Invalid value '$input'. Expected one of: $allowed")
    ).create()
}
