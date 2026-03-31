package io.github.kraftlin.message.velocity

import com.velocitypowered.api.command.CommandSource
import io.github.kraftlin.message.ClickableMessage
import io.github.kraftlin.message.ClickableText
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import io.github.kraftlin.message.message as buildMessage

public fun CommandSource.message(block: ClickableMessage.() -> Unit): Unit =
    sendMessage(buildMessage(block))

public fun CommandSource.message(text: String, color: TextColor? = null) {
    if (color == null) {
        sendMessage(buildMessage(text))
    } else {
        sendMessage(buildMessage(text, color))
    }
}

public fun CommandSource.message(text: String, block: ClickableText.() -> Unit) {
    sendMessage(buildMessage(text, block))
}

public fun CommandSource.message(component: Component, block: ClickableText.() -> Unit = {}) {
    sendMessage(buildMessage(component, block))
}
