package io.github.kraftlin.message.bungee

import io.github.kraftlin.message.ClickableMessage
import io.github.kraftlin.message.ClickableText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.CommandSender
import io.github.kraftlin.message.message as buildMessage

/** Builds and sends a message to this [CommandSender] using the Kraftlin message DSL. */
public fun CommandSender.message(block: ClickableMessage.() -> Unit): Unit =
    sendMessage(*BungeeComponentSerializer.get().serialize(buildMessage(block)))

/** Sends a simple text message, optionally colored with [color]. */
public fun CommandSender.message(text: String, color: TextColor? = null) {
    if (color == null) {
        sendMessage(*BungeeComponentSerializer.get().serialize(buildMessage(text)))
    } else {
        sendMessage(*BungeeComponentSerializer.get().serialize(buildMessage(text, color)))
    }
}

/** Builds and sends a message starting from [text] with additional formatting via [block]. */
public fun CommandSender.message(text: String, block: ClickableText.() -> Unit) {
    sendMessage(*BungeeComponentSerializer.get().serialize(buildMessage(text, block)))
}

/** Builds and sends a message starting from an existing [component] with additional formatting via [block]. */
public fun CommandSender.message(component: Component, block: ClickableText.() -> Unit = {}) {
    sendMessage(*BungeeComponentSerializer.get().serialize(buildMessage(component, block)))
}
