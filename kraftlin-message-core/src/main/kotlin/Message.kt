package io.github.kraftlin.message

import net.kyori.adventure.text.TextComponent

/**
 * A basic formatted Chat Message.
 *
 * The text can be formatted, but no interactions are allowed. Use [ClickableMessage] to allow user interaction with the
 * message. Formatting is inherited to all text components but can be overwritten by individual text components. To
 * start building a message use [message] and declarative style kotlin syntax.
 */
@ChatMarker
public class Message private constructor(
    //The base element handles message wide formatting, as it inherits its properties to all children
    private val baseElement: Text
) : Stylable by baseElement {

    private val textElements = mutableListOf<Text>()

    // Replaces the primary constructor to avoid populating with anything but an empty baseElement.
    // It is required as an argument to make use of the implementation by delegation feature.
    internal constructor() : this(Text(""))

    /**
     * Adds a new text component to the message.
     *
     * @param text The component's text to display.
     * @param init Function to apply formatting in a declarative style.
     */
    public fun text(text: String, init: Text.() -> Unit = {}) {
        val textComponent = Text(text)
        textComponent.init()
        textElements.add(textComponent)
    }

    /**
     * Adds a new text component to the message.
     *
     * @param text The component's text to display.
     * @param init Function to apply formatting in a declarative style.
     */
    public fun text(text: TextComponent, init: Text.() -> Unit = {}) {
        val textComponent = Text(text)
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
    public fun legacyText(legacyText: String, init: Text.() -> Unit = {}): Unit =
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
