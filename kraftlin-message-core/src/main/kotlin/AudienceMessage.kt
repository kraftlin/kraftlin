package io.github.kraftlin.message

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor

// Private builders that unambiguously call the top-level message() functions.
// Inside Audience extension bodies, a same-package import alias still resolves to the
// extension (self-reference), so we use these free-standing helpers instead.
private fun build(init: ClickableMessage.() -> Unit): Component = message(init)
private fun build(text: String): Component = message(text)
private fun build(text: String, color: TextColor): Component = message(text, color)
private fun build(text: String, init: ClickableText.() -> Unit): Component = message(text, init)
private fun build(component: Component, init: ClickableText.() -> Unit): Component = message(component, init)

/**
 * Builds and sends a chat message to this [Audience].
 *
 * Works on any platform whose sender implements [Audience] (Paper, Velocity, …).
 */
public fun Audience.message(block: ClickableMessage.() -> Unit): Unit =
    sendMessage(build(block))

/**
 * Builds and sends a simple text message to this [Audience].
 */
public fun Audience.message(text: String, color: TextColor? = null) {
    if (color == null) {
        sendMessage(build(text))
    } else {
        sendMessage(build(text, color))
    }
}

/**
 * Builds and sends a styled text message to this [Audience].
 */
public fun Audience.message(text: String, block: ClickableText.() -> Unit) {
    sendMessage(build(text, block))
}

/**
 * Builds and sends a component-based message to this [Audience].
 */
public fun Audience.message(component: Component, block: ClickableText.() -> Unit = {}) {
    sendMessage(build(component, block))
}
