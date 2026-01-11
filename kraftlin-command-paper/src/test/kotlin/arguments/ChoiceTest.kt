package io.github.kbrigx.paper.arguments

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.github.kbrigx.core.KContext
import io.github.kbrigx.paper.PaperKBrigx
import io.github.kbrigx.paper.PaperSource
import io.github.kbrigx.paper.executes
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class ChoiceTest {

    @Suppress("unused")
    enum class TestEnum {
        VALUE_A,
        VALUE_B
    }

    @Test
    fun `test choice helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = "testValue"

        every { raw.getArgument("arg", String::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.choice("arg"))
    }

    @Test
    fun `test choice DSL`() {
        val node = PaperKBrigx.command("test") {
            choice("myChoice", listOf("a", "b")) {
                executes { _, _ -> }
            }
        }

        val arg = node.children.first()
        assertEquals("myChoice", arg.name)
        // ChoiceArgumentType is internal, but we can check if it's there
        // and if it produces suggestions
        val suggestions = arg.listSuggestions(mockk(), SuggestionsBuilder("", 0)).get()
        val tokens = suggestions.list.map { it.text }
        assertEquals(listOf("a", "b"), tokens)
    }

    @Test
    fun `test enum helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = TestEnum.VALUE_A

        every { raw.getArgument("arg", TestEnum::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.enum<TestEnum>("arg"))
    }

    @Test
    fun `test enum DSL`() {
        val node = PaperKBrigx.command("test") {
            enum<TestEnum>("myEnum") {
                executes { _, _ -> }
            }
        }

        val arg = node.children.first()
        assertEquals("myEnum", arg.name)
        val suggestions = arg.listSuggestions(mockk(), SuggestionsBuilder("", 0)).get()
        val tokens = suggestions.list.map { it.text }
        assertEquals(listOf("value_a", "value_b"), tokens)
    }
}
