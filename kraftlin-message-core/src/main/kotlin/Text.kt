package io.github.kraftlin.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * Compatibility extension for Adventure 4.25.0 which doesn't have Component.toBuilder()
 * TODO: replace with Component.toBuilder() once paper updates adventure to 4.26
 */
private fun Component.toBuilderCompat(): ComponentBuilder<*, *> = text().append(this)

/**
 * A text part of a [Message].
 *
 * If a formatting option is not specified, the value of the containing message is applied.
 *
 * @constructor Creates a message component with the given text.
 * @param builder The component builder.
 */
@ChatMarker
public open class Text(internal val builder: ComponentBuilder<*, *>) : Stylable {

    public constructor(component: Component) : this(component.toBuilderCompat())

    public constructor(text: String) : this(text().content(text))

    override fun color(color: TextColor) {
        builder.color(color)
    }

    override fun underlined(underlined: Boolean) {
        builder.decoration(TextDecoration.UNDERLINED, underlined)
    }

    override fun italic(italic: Boolean) {
        builder.decoration(TextDecoration.ITALIC, italic)
    }

    override fun bold(bold: Boolean) {
        builder.decoration(TextDecoration.BOLD, bold)
    }

    override fun strikeThrough(strikeThrough: Boolean) {
        builder.decoration(TextDecoration.STRIKETHROUGH, strikeThrough)
    }

    override fun obfuscated(obfuscated: Boolean) {
        builder.decoration(TextDecoration.OBFUSCATED, obfuscated)
    }
}
