package io.github.kbrigx.paper.arguments

import io.github.kbrigx.core.argument
import io.github.kbrigx.paper.PaperArgumentNode
import io.github.kbrigx.paper.PaperContext
import io.github.kbrigx.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.SignedMessageResolver
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import java.util.concurrent.CompletableFuture


/**
 * Adds a [SignedMessage] argument. This preserves signatures from the client, if supported.
 * This argument type is useful for commands that accept chat components from players - such as chat or broadcast commands.
 *
 * Documentation:
 * [Signed Message Argument](https://docs.papermc.io/paper/dev/command-api/arguments/adventure/#signed-message-argument)
 * [Signed Messages](https://docs.papermc.io/paper/dev/component-api/signed-messages/)
 *
 * @see [PaperContext.signedMessage]
 */
public fun PaperLiteralNode.signedMessage(
    name: String,
    block: PaperArgumentNode<SignedMessageResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.signedMessage(), block)

/**
 * Returns the parsed [SignedMessage], preserving signatures if provided by the client.
 *
 * Documentation:
 * [Signed Message Argument](https://docs.papermc.io/paper/dev/command-api/arguments/adventure/#signed-message-argument)
 * [Signed Messages](https://docs.papermc.io/paper/dev/component-api/signed-messages/)
 *
 * @return A future that completes with the resolved signed message. Don't block on the main thread!
 * @throws [com.mojang.brigadier.exceptions.CommandSyntaxException] if the argument could not be resolved. The exception is reported to the command sender as a Brigadier command error (with input highlighting).
 * @see [PaperLiteralNode.signedMessage]
 */
public fun PaperContext.signedMessage(name: String): CompletableFuture<SignedMessage> =
    rawContext.getArgument<SignedMessageResolver>(name, SignedMessageResolver::class.java)
        .resolveSignedMessage(name, rawContext)


/**
 * Adds a formatted chat [Component] argument to this command.
 *
 * A component argument allows structured and formatted text to be passed to commands.
 *
 * Documentation: [Component Argument](https://docs.papermc.io/paper/dev/command-api/arguments/adventure/#component-argument)
 * @see [PaperLiteralNode.component]
 */
public fun PaperLiteralNode.component(
    name: String,
    block: PaperArgumentNode<Component>.() -> Unit,
): Unit = argument(name, ArgumentTypes.component(), block)

/**
 * Retrieves the value of a [Component] argument from the command context.
 *
 * Documentation: [Component Argument](https://docs.papermc.io/paper/dev/command-api/arguments/adventure/#component-argument)
 * @see [PaperLiteralNode.component]
 */
public fun PaperContext.component(name: String): Component =
    rawContext.getArgument<Component>(name, Component::class.java)


/**
 * Adds a [Style] argument to this command, which can be applied using [Component.style].
 *
 * Documentation: [Style Argument](https://docs.papermc.io/paper/dev/command-api/arguments/adventure/#adventure-style-argument)
 * @see [PaperLiteralNode.style]
 */
public fun PaperLiteralNode.style(
    name: String,
    block: PaperArgumentNode<Style>.() -> Unit,
): Unit = argument(name, ArgumentTypes.style(), block)

/**
 * Retrieves the value of a [Style] argument from the command context.
 *
 * Documentation: [Style Argument](https://docs.papermc.io/paper/dev/command-api/arguments/adventure/#adventure-style-argument)
 * @see [PaperLiteralNode.style]
 */
public fun PaperContext.style(name: String): Style = rawContext.getArgument<Style>(name, Style::class.java)


public fun PaperLiteralNode.namedColor(
    name: String,
    block: PaperArgumentNode<NamedTextColor>.() -> Unit,
): Unit = argument(name, ArgumentTypes.namedColor(), block)

public fun PaperContext.namedColor(name: String): NamedTextColor =
    rawContext.getArgument<NamedTextColor>(name, NamedTextColor::class.java)


public fun PaperLiteralNode.hexColor(
    name: String,
    block: PaperArgumentNode<TextColor>.() -> Unit,
): Unit = argument(name, ArgumentTypes.hexColor(), block)

public fun PaperContext.hexColor(name: String): TextColor =
    rawContext.getArgument<TextColor>(name, TextColor::class.java)
