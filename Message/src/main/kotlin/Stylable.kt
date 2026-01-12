package me.minoneer.minecraft.message

import net.kyori.adventure.text.format.TextColor

/**
 * A part of a message to which text formatting can be applied.
 */
@ChatMarker
public interface Stylable {

    /**
     * Sets the color for the component.
     *
     * @param color the text color.
     */
    public fun color(color: TextColor)

    /**
     * Sets the font to bold.
     */
    public fun bold(bold: Boolean = true)

    /**
     * Sets the font to italic.
     */
    public fun italic(italic: Boolean = true)

    /**
     * Underlines the text.
     */
    public fun underlined(underlined: Boolean = true)

    /**
     * Strikes the text through.
     */
    public fun strikeThrough(strikeThrough: Boolean = true)

    /**
     * Obfuscates text by switching each letter between multiple characters. Will result in non-readable text.
     */
    public fun obfuscated(obfuscated: Boolean = true)
}
