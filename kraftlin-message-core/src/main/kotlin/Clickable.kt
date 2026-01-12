package io.github.kraftlin.message

import net.kyori.adventure.text.event.HoverEventSource

/**
 * A part of a message which supports mouse interaction.
 */
@ChatMarker
public interface Clickable {

    /**
     * Runs a command when left-clicking on the text as the user who clicked the message.
     *
     * Cannot be combined with [suggestCommand], [copyToClipboard] or [openUrl].
     *
     * @param command The command to run.
     */
    public fun runCommand(command: String)

    /**
     * Inserts a command into the client's chat bar of the user who clicked the message.
     *
     * The suggestion replaces anything already in the chat bar. Use [insert] to append to the current chat input.
     * Cannot be combined with [runCommand], [copyToClipboard] or [openUrl].
     *
     * @param command The command to suggest.
     */
    public fun suggestCommand(command: String)

    /**
     * Opens the "open URL" dialog when the user clicks on the message.
     *
     * Cannot be combined with [runCommand], [copyToClipboard] or [suggestCommand].
     *
     * @param url The URL to open.
     */
    public fun openUrl(url: String)

    /**
     * Copies the given text to the users clipboard when they click on the message.
     *
     * Cannot be combined with [runCommand], [openUrl] or [suggestCommand].
     *
     * @param text The text to copy.
     */
    public fun copyToClipboard(text: String)

    /**
     * Sets the hover message displayed when the user hovers the mouse over the text.
     *
     * See the overloaded method for a simplified way of setting a single text element as hover-message.
     *
     * @param init A function applied to the new message component. Used for declarative style message construction.
     */
    public fun hoverMessage(init: Message.() -> Unit = {})

    /**
     * Sets the text displayed as hover-message when the user hovers the mouse over the text.
     *
     * See the overloaded method for setting a fully composed message as hover-message.
     *
     * @param message The text to display
     * @param init A function applied to the text for formatting. Used for declarative style text formatting.
     */
    public fun hoverMessage(message: String, init: Text.() -> Unit = {})

    /**
     * Sets the source for the content when hovering over the message. Can be many things,
     * e.g., a text component, item, entity, etc.
     *
     * @param eventSource The source of the type to display when hovering
     */
    public fun hoverEvent(eventSource: HoverEventSource<*>)

    /**
     * Sets the text appended to the client's text input when the user shift + left-clicks a message.
     *
     * The suggestion is appended to the current text in the chat input. Use [suggestCommand] to completely replace input.
     *
     * @param text The text to insert.
     */
    public fun insert(text: String)
}
