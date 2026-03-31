package io.github.kraftlin.message.paper

import io.github.kraftlin.message.ClickableMessage
import io.github.kraftlin.message.ClickableText
import io.github.kraftlin.message.message
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender

// Private builders to avoid self-reference through Audience.message() in the same package tree.
private fun build(init: ClickableMessage.() -> Unit): Component = message(init)
private fun build(text: String): Component = message(text)
private fun build(text: String, color: TextColor): Component = message(text, color)
private fun build(text: String, init: ClickableText.() -> Unit): Component = message(text, init)
private fun build(component: Component, init: ClickableText.() -> Unit): Component = message(component, init)


@Deprecated(
    "Use Audience.message() from kraftlin-message-core instead.",
    ReplaceWith("message(block)"),
)
public fun CommandSender.message(block: ClickableMessage.() -> Unit): Unit =
    sendMessage(build(block))

@Deprecated(
    "Use Audience.message() from kraftlin-message-core instead.",
    ReplaceWith("message(text, color)"),
)
public fun CommandSender.message(text: String, color: TextColor? = null) {
    if (color == null) {
        sendMessage(build(text))
    } else {
        sendMessage(build(text, color))
    }
}

@Deprecated(
    "Use Audience.message() from kraftlin-message-core instead.",
    ReplaceWith("message(text, block)"),
)
public fun CommandSender.message(text: String, block: ClickableText.() -> Unit) {
    sendMessage(build(text, block))
}

@Deprecated(
    "Use Audience.message() from kraftlin-message-core instead.",
    ReplaceWith("message(component, block)"),
)
public fun CommandSender.message(component: Component, block: ClickableText.() -> Unit = {}) {
    sendMessage(build(component, block))
}
