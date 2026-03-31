package io.github.kraftlin.message.bungee

import io.github.kraftlin.message.ClickableMessage
import io.github.kraftlin.message.ClickableText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer
import net.md_5.bungee.api.CommandSender
import io.github.kraftlin.message.message as buildMessage

public fun CommandSender.message(block: ClickableMessage.() -> Unit): Unit =
    sendMessage(*BungeeComponentSerializer.get().serialize(buildMessage(block)))

public fun CommandSender.message(text: String, color: TextColor? = null) {
    if (color == null) {
        sendMessage(*BungeeComponentSerializer.get().serialize(buildMessage(text)))
    } else {
        sendMessage(*BungeeComponentSerializer.get().serialize(buildMessage(text, color)))
    }
}

public fun CommandSender.message(text: String, block: ClickableText.() -> Unit) {
    sendMessage(*BungeeComponentSerializer.get().serialize(buildMessage(text, block)))
}

public fun CommandSender.message(component: Component, block: ClickableText.() -> Unit = {}) {
    sendMessage(*BungeeComponentSerializer.get().serialize(buildMessage(component, block)))
}
