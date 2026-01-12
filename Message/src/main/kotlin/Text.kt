package me.minoneer.minecraft.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * A text part of a [Message].
 *
 * If a formatting option is not specified, the value of the containing message is applied.
 *
 * @constructor Creates a message component with the given text.
 * @param textComponent The display text of the component.
 */
@ChatMarker
public open class Text(internal var textComponent: TextComponent) : Stylable {

    /**
     * Creates a message component with the given message as text.
     *
     * @param text The message components to use as text.
     */
    public constructor(text: String) : this(Component.text(text))

    override fun color(color: TextColor) {
        textComponent = textComponent.color(color)
    }

    override fun underlined(underlined: Boolean) {
        textComponent = textComponent.decoration(TextDecoration.UNDERLINED, underlined)
    }

    override fun italic(italic: Boolean) {
        textComponent = textComponent.decoration(TextDecoration.ITALIC, italic)
    }

    override fun bold(bold: Boolean) {
        textComponent = textComponent.decoration(TextDecoration.BOLD, bold)
    }

    override fun strikeThrough(strikeThrough: Boolean) {
        textComponent = textComponent.decoration(TextDecoration.STRIKETHROUGH, strikeThrough)
    }

    override fun obfuscated(obfuscated: Boolean) {
        textComponent = textComponent.decoration(TextDecoration.OBFUSCATED, obfuscated)
    }
}
