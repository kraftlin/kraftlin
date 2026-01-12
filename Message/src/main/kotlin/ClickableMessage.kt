package me.minoneer.minecraft.message

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor

/**
 * A chat message the user can interact with.
 *
 * Clickable messages allow setting mouse interactions by clicking, shift + clicking and hovering. Formatting and actions
 * are inherited to all text components but can be overwritten by individual text components. To start building a
 * message use [message] and declarative style kotlin syntax.
 */
@ChatMarker
public class ClickableMessage private constructor(
    //The base element handles message wide formatting, as it inherits its properties to all children
    private val baseElement: ClickableText
) : Stylable by baseElement, Clickable by baseElement {

    private val textElements = mutableListOf<ClickableText>()

    // Replaces the primary constructor to avoid populating with anything but an empty baseElement.
    // It is required as an argument to make use of the implementation by delegation feature.
    internal constructor() : this(ClickableText(""))

    /**
     * Adds a new text component to the message.
     *
     * @param text The component's text to display.
     * @param init Function to apply formatting in a declarative style.
     */
    public fun text(text: String, init: ClickableText.() -> Unit = {}) {
        val textComponent = ClickableText(text)
        textComponent.init()
        textElements.add(textComponent)
    }

    /**
     * Convenience method to build a simple colorized message. For more flexible messages, use the overloaded version.
     *
     * @param text The message text.
     * @param color The text color.
     */
    public fun text(text: String, color: TextColor) {
        text(text) { color(color) }
    }

    /**
     * Adds a new text component to the message.
     *
     * @param text The component's text to display.
     * @param init Function to apply formatting in a declarative style.
     */
    public fun text(text: TextComponent, init: ClickableText.() -> Unit = {}) {
        val textComponent = ClickableText(text)
        textComponent.init()
        textElements.add(textComponent)
    }

    /**
     * Adds a new text component from a legacy message formatted with § and color codes.
     * Useful for things such as player display names.
     *
     * @param legacyText The legacy text to convert display.
     * @param init       Function to apply formatting in a declarative style.
     */
    public fun legacyText(legacyText: String, init: ClickableText.() -> Unit = {}): Unit =
        text(fromLegacyMessage(legacyText), init)

    /**
     * Converts the message to a [TextComponent] to use with the Chat API.
     *
     * @return The message encoded as the Chat APIs message component.
     */
    public fun toChatMessage(): TextComponent {
        val result = baseElement.textComponent
        return result.children(textElements.map(Text::textComponent))
    }
}
