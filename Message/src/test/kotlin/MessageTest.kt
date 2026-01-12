package me.minoneer.minecraft.message

import net.kyori.adventure.text.format.NamedTextColor.*
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.intellij.lang.annotations.Language
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode
import kotlin.test.Test
import kotlin.test.assertEquals

public class MessageTest {

    @Test
    public fun `simple one-component message`() {
        val message = message("Test")

        @Language("JSON") val expected = "\"Test\""
        assertEquals(expected, GsonComponentSerializer.gson().serialize(message))
    }

    @Test
    public fun `simple one-component message with color`() {
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
    public fun `plain one component message with complex constructor`() {
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
    public fun `pain multi component message`() {
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
    public fun `colored multi component message`() {
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
    public fun `single component with text formatting`() {
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
    public fun `run command action`() {
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
                    "clickEvent": {
                        "action": "run_command",
                        "value": "/bar"
                    }
                }
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    public fun `suggest command action`() {
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
                    "clickEvent": {
                        "action": "suggest_command",
                        "value": "/bar"
                    }
                }
            ],
            "text": ""
        }"""
        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    public fun `copy to clipboard action`() {
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
                    "clickEvent": {
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
    public fun `plain hover text`() {
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
                    "hoverEvent": {
                        "action": "show_text",
                        "contents": "Bar"
                    }
                }
            ],
            "text": ""
        }
        """

        assertEquals(expected, GsonComponentSerializer.gson().serialize(message), JSONCompareMode.STRICT)
    }

    @Test
    public fun `formatted hover text`() {
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
                    "hoverEvent": {
                        "action": "show_text",
                        "contents": {
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
    public fun `multi component hover text`() {
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
                    "hoverEvent": {
                        "action": "show_text",
                        "contents": {
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
    public fun `open URL action`() {
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
                    "clickEvent": {
                        "action": "open_url",
                        "value": "https://www.google.de/"
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
    public fun `insertion action`() {
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
    public fun `formatting hirachy`() {
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
    public fun `colored single component message`() {
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
    public fun `simple colored text component`() {
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
    public fun `formatted single component message`() {
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
