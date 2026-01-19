package io.github.kraftlin.message

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentBuilder
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEventSource

/**
 * A text part of a [ClickableMessage].
 *
 * If a formatting option or action is not specified, the value of the containing message is applied.
 *
 * @constructor Creates a message component with the given text.
 * @param builder The display text of the component.
 */
@ChatMarker
public class ClickableText(builder: ComponentBuilder<*, *>) : Text(builder), Stylable, Clickable {

    public constructor(component: Component) : this(component.toBuilder())

    public constructor(text: String) : this(Component.text().content(text))

    override fun runCommand(command: String) {
        if (builder.build().clickEvent() != null) {
            throw IllegalStateException("A click action is already defined")
        }
        builder.clickEvent(ClickEvent.runCommand(command))
    }

    override fun suggestCommand(command: String) {
        if (builder.build().clickEvent() != null) {
            throw IllegalStateException("A click action is already defined")
        }
        builder.clickEvent(ClickEvent.suggestCommand(command))
    }

    override fun openUrl(url: String) {
        if (builder.build().clickEvent() != null) {
            throw IllegalStateException("A click action is already defined")
        }
        builder.clickEvent(ClickEvent.openUrl(url))
    }

    override fun copyToClipboard(text: String) {
        if (builder.build().clickEvent() != null) {
            throw IllegalStateException("A click action is already defined")
        }
        builder.clickEvent(ClickEvent.copyToClipboard(text))
    }

    override fun hoverMessage(message: String, init: Text.() -> Unit) {
        val text = Text(message)
        text.init()
        hoverEvent(text.builder.build())
    }

    override fun hoverMessage(init: Message.() -> Unit) {
        val message = Message()
        message.init()
        hoverEvent(message.toChatMessage())
    }

    override fun hoverEvent(eventSource: HoverEventSource<*>) {
        if (builder.build().hoverEvent() != null) {
            throw IllegalStateException("A hover action is already defined")
        }
        builder.hoverEvent(eventSource)
    }

    override fun insert(text: String) {
        builder.insertion(text)
    }
}
