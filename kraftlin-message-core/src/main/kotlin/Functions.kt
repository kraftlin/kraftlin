package io.github.kraftlin.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

/**
 * Builds a new chat message.
 *
 * The API is designed to be used with declarative style kotlin syntax. Example:
 *
 *      message {
 *          text("Foo") {
 *              color(GREEN)
 *              bold()
 *              runCommand("/bar")
 *          }
 *      }
 *
 *  This results in a message displaying "Foo" in bold green font and executing the command "/bar" when the user clicks
 *  on it.
 *
 *  @param init The function to construct the message for declarative style syntax.
 */
public fun message(init: ClickableMessage.() -> Unit): Component {
    val message = ClickableMessage()
    message.init()
    return message.toChatMessage()
}

/**
 * Convenience method to build a simple single component message. For more flexible messages, use the overloaded version.
 *
 * @param text The message text
 * @param init A function to apply text formatting in declarative syntax.
 */
public fun message(text: String, init: ClickableText.() -> Unit = {}): Component {
    val textComponent = ClickableText(text)
    textComponent.init()
    return textComponent.builder.build()
}

/**
 * Convenience method to build a simple single component message. For more flexible messages, use the overloaded version.
 *
 * @param component The component to use as base
 * @param init A function to apply text formatting in declarative syntax.
 */
public fun message(component: Component, init: ClickableText.() -> Unit = {}): Component {
    val textComponent = ClickableText(component)
    textComponent.init()
    return textComponent.builder.build()
}

/**
 * Convenience method to build a simple colorized message. For more flexible messages, use the overloaded version.
 *
 * @param text The message text.
 * @param color The text color.
 */
public fun message(text: String, color: TextColor): Component {
    val textComponent = Text(text)
    textComponent.color(color)
    return textComponent.builder.build()
}

/**
 * Converts a complex message to a single string with formatting characters as used in old Minecraft versions.
 *
 * @return A single human-readable string with '§a'-style formatting.
 */
public fun Component.toLegacyMessage(): String {
    return LegacyComponentSerializer.legacySection().serialize(this)
}

public fun fromLegacyMessage(legacy: String): TextComponent {
    return LegacyComponentSerializer.legacySection().deserialize(legacy)
}
