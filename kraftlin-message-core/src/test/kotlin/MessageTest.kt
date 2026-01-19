package io.github.kraftlin.message

import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.intellij.lang.annotations.Language
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageTest {

    @Test
    fun `simple one-component message`() {
        val message = message("Test")

        @Language("JSON") val expected = "\"Test\""
        assertEquals(expected, GsonComponentSerializer.gson().serialize(message))
    }

    @Test
    fun `simple one-component message with color`() {
        val message = message("Test", DARK_BLUE)

        @Language("JSON") val expected = """
        {
            "text": "Test",
            "color": "dark_blue"
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `plain one component message with complex constructor`() {
        val message = message {
            text("Hello World")
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                "Hello World"
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `pain multi component message`() {
        val message = message {
            text("Hello ")
            text("World! ")
            text("There is much to do")
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                "Hello ",
                "World! ",
                "There is much to do"
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `colored multi component message`() {
        val message = message {
            text("Hello World! ") {
                color(GOLD)
            }
            text("Come Back ") {
                color(DARK_PURPLE)
            }
            text("to me")
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "color": "gold",
                    "text": "Hello World! "
                },
                {
                    "color": "dark_purple",
                    "text": "Come Back "
                },
                "to me"
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `single component with text formatting`() {
        val message = message {
            text("Foo Bar") {
                strikeThrough()
                underlined()
                obfuscated()
                bold()
                italic()
            }
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "text": "Foo Bar",
                    "bold": true,
                    "strikethrough": true,
                    "underlined": true,
                    "obfuscated": true,
                    "italic": true
                }
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `translatable component message`() {
        val translatable = net.kyori.adventure.text.Component.translatable("chat.type.text")
        val message = message(translatable) {
            color(RED)
        }

        @Language("JSON") val expected = """
        {
            "translate": "chat.type.text",
            "color": "red"
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `nested translatable component`() {
        val translatable = net.kyori.adventure.text.Component.translatable("chat.type.text")
        val message = message {
            text(translatable) {
                bold()
            }
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "bold": true,
                    "translate": "chat.type.text"
                }
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `run command action`() {
        val message = message {
            text("Foo") {
                runCommand("/bar")
            }
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "text": "Foo",
                    "click_event": {
                        "action": "run_command",
                        "command": "/bar"
                    }
                }
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `suggest command action`() {
        val message = message {
            text("Foo") {
                suggestCommand("/bar")
            }
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "text": "Foo",
                    "click_event": {
                        "action": "suggest_command",
                        "command": "/bar"
                    }
                }
            ],
            "text": ""
        }"""
        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `copy to clipboard action`() {
        val message = message {
            text("Foo") {
                copyToClipboard("bar")
            }
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "text": "Foo",
                    "click_event": {
                        "action": "copy_to_clipboard",
                        "value": "bar"
                    }
                }
            ],
            "text": ""
        }"""
        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `plain hover text`() {
        val message = message {
            text("Foo") {
                hoverMessage("Bar")
            }
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "text": "Foo",
                    "hover_event": {
                        "action": "show_text",
                        "value": "Bar"
                    }
                }
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `formatted hover text`() {
        val message = message {
            text("Foo") {
                hoverMessage("Bar") {
                    color(BLUE)
                    underlined()
                    strikeThrough()
                    bold()
                    italic()
                    obfuscated()
                }
            }
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "text": "Foo",
                    "hover_event": {
                        "action": "show_text",
                        "value": {
                            "text": "Bar",
                            "color": "blue",
                            "underlined": true,
                            "strikethrough": true,
                            "bold": true,
                            "italic": true,
                            "obfuscated": true
                        }
                    }
                }
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `multi component hover text`() {
        val message = message {
            text("Foo") {
                hoverMessage {
                    text("First ")
                    text("Second") {
                        color(BLACK)
                    }
                }
            }
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "text": "Foo",
                    "hover_event": {
                        "action": "show_text",
                        "value": {
                            "text": "",
                            "extra": [
                                "First ",
                                {
                                    "text": "Second",
                                    "color": "black"
                                }
                            ]
                        }
                    }
                }
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `open URL action`() {
        val message = message {
            text("google.de") {
                underlined()
                openUrl("https://www.google.de/")
            }
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "text": "google.de",
                    "click_event": {
                        "action": "open_url",
                        "url": "https://www.google.de/"
                    },
                    "underlined": true
                }
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `insertion action`() {
        val message = message {
            text("Foo") {
                insert("Bar")
            }
        }

        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "text": "Foo",
                    "insertion": "Bar"
                }
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `formatting hirachy`() {
        val message = message {
            text("Foo")
            text("bar")
            text("Something") {
                color(GRAY)
            }
            text("Else")

            color(YELLOW)
            insert("Insertion")
        }

        @Language("JSON") val expected = """
        {
            "color": "yellow",
            "insertion": "Insertion",
            "extra": [
                "Foo",
                "bar",
                {
                    "text": "Something",
                    "color": "gray"
                },
                "Else"
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `colored single component message`() {
        val message = message("Test", DARK_PURPLE)
        @Language("JSON") val expected = """
        {
            "text": "Test",
            "color": "dark_purple"
        }
        """
        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `simple colored text component`() {
        val message = message { text("Test", DARK_PURPLE) }
        @Language("JSON") val expected = """
        {
            "extra": [
                {
                    "text": "Test",
                    "color": "dark_purple"
                }
            ],
            "text": ""
        }
        """
        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    fun `formatted single component message`() {
        val message = message("Text") {
            strikeThrough()
            insert("Insertion")
        }
        @Language("JSON") val expected = """
        {
            "text": "Text",
            "insertion": "Insertion",
            "strikethrough": true
        }
        """
        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }
}
