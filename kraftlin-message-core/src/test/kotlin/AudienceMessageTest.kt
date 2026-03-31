package io.github.kraftlin.message

import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.Test

class AudienceMessageTest {

    private fun captureMessage(block: Audience.() -> Unit): Component {
        val captured = slot<Component>()
        val audience = mockk<Audience>(relaxed = true)
        audience.block()
        verify { audience.sendMessage(capture(captured)) }
        return captured.captured
    }

    private fun assertJson(expected: String, component: Component) {
        assertEquals(expected, GsonComponentSerializer.gson().serialize(component), JSONCompareMode.STRICT)
    }

    @Test
    fun `message with block sends clickable message`() {
        val component = captureMessage {
            message {
                text("Hello") {
                    color(GOLD)
                }
            }
        }

        assertJson(
            """
            {
                "extra": [
                    {
                        "text": "Hello",
                        "color": "gold"
                    }
                ],
                "text": ""
            }
            """,
            component,
        )
    }

    @Test
    fun `message with plain text`() {
        val component = captureMessage {
            message("Hello")
        }

        assertJson("\"Hello\"", component)
    }

    @Test
    fun `message with text and color`() {
        val component = captureMessage {
            message("Hello", RED)
        }

        assertJson(
            """
            {
                "text": "Hello",
                "color": "red"
            }
            """,
            component,
        )
    }

    @Test
    fun `message with text and null color`() {
        val component = captureMessage {
            message("Hello", null)
        }

        assertJson("\"Hello\"", component)
    }

    @Test
    fun `message with text and formatting block`() {
        val component = captureMessage {
            message("Hello") {
                bold()
                color(GREEN)
            }
        }

        assertJson(
            """
            {
                "text": "Hello",
                "bold": true,
                "color": "green"
            }
            """,
            component,
        )
    }

    @Test
    fun `message with component and formatting block`() {
        val base = Component.text("Base")
        val component = captureMessage {
            message(base) {
                italic()
            }
        }

        // toBuilderCompat() wraps the component: text("").append(base)
        assertJson(
            """
            {
                "extra": [
                    "Base"
                ],
                "text": "",
                "italic": true
            }
            """,
            component,
        )
    }

    @Test
    fun `message with component and empty block`() {
        val base = Component.text("Passthrough")
        val component = captureMessage {
            message(base)
        }

        // toBuilderCompat() wraps the component: text("").append(base)
        assertJson(
            """
            {
                "extra": [
                    "Passthrough"
                ],
                "text": ""
            }
            """,
            component,
        )
    }
}
