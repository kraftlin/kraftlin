package io.github.kraftlin.message.paper

import io.github.kraftlin.message.ClickableMessage
import io.github.kraftlin.message.ClickableText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender
import io.github.kraftlin.message.message as buildMessage


public fun CommandSender.message(block: ClickableMessage.() -> Unit): Unit =
    sendMessage(buildMessage(block))

public fun CommandSender.message(text: String, color: TextColor? = null) {
    if (color == null) {
        sendMessage(buildMessage(text))
    } else {
        sendMessage(buildMessage(text, color))
    }
}

public fun CommandSender.message(text: String, block: ClickableText.() -> Unit) {
    sendMessage(buildMessage(text, block))
}

public fun CommandSender.message(component: Component, block: ClickableText.() -> Unit = {}) {
    sendMessage(buildMessage(component, block))
}
