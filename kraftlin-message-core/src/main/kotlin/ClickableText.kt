package io.github.kraftlin.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEventSource

/**
 * A text part of a [ClickableMessage].
 *
 * If a formatting option or action is not specified, the value of the containing message is applied.
 *
 * @constructor Creates a message component with the given text.
 * @param text The display text of the component.
 */
@ChatMarker
public class ClickableText(text: TextComponent) : Text(text), Stylable, Clickable {

    /**
     * Creates a message component with the given message as text.
     *
     * @param text The message components to use as text.
     */
    public constructor(text: String) : this(Component.text(text))

    override fun runCommand(command: String) {
        if (textComponent.clickEvent() != null) {
            throw IllegalStateException("A click action is already defined")
        }
        textComponent = textComponent.clickEvent(ClickEvent.runCommand(command))
    }

    override fun suggestCommand(command: String) {
        if (textComponent.clickEvent() != null) {
            throw IllegalStateException("A click action is already defined")
        }
        textComponent = textComponent.clickEvent(ClickEvent.suggestCommand(command))
    }

    override fun openUrl(url: String) {
        if (textComponent.clickEvent() != null) {
            throw IllegalStateException("A click action is already defined")
        }
        textComponent = textComponent.clickEvent(ClickEvent.openUrl(url))
    }

    override fun copyToClipboard(text: String) {
        if (textComponent.clickEvent() != null) {
            throw IllegalStateException("A click action is already defined")
        }
        textComponent = textComponent.clickEvent(ClickEvent.copyToClipboard(text))
    }

    override fun hoverMessage(message: String, init: Text.() -> Unit) {
        val text = Text(message)
        text.init()
        hoverEvent(text.textComponent)
    }

    override fun hoverMessage(init: Message.() -> Unit) {
        val message = Message()
        message.init()
        hoverEvent(message.toChatMessage())
    }

    override fun hoverEvent(eventSource: HoverEventSource<*>) {
        if (textComponent.hoverEvent() != null) {
            throw IllegalStateException("A hover action is already defined")
        }
        textComponent = textComponent.hoverEvent(eventSource)
    }

    override fun insert(text: String) {
        textComponent = textComponent.insertion(text)
    }
}
