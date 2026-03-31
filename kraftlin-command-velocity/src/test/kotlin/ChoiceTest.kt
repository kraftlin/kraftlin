package io.github.kraftlin.command.velocity

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.github.kraftlin.command.KContext
import io.github.kraftlin.command.executes
import io.github.kraftlin.command.velocity.arguments.choice
import io.github.kraftlin.command.velocity.arguments.enum
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChoiceTest {

    private enum class GameMode { SURVIVAL, CREATIVE, ADVENTURE }

    @Test
    fun `choice registers word argument with suggestions`() {
        val cmd = kraftlinCommand("test") {
            choice("mode", listOf("a", "b", "c")) {
                executes { }
            }
        }

        val arg = cmd.node.getChild("mode")
        assertNotNull(arg, "choice argument should exist")
    }

    @Test
    fun `choice extraction returns valid value`() {
        val raw = mockk<com.mojang.brigadier.context.CommandContext<VelocitySource>>()
        every { raw.source } returns mockk()
        every { raw.getArgument("mode", String::class.java) } returns "creative"

        // StringArgumentType.getString delegates to getArgument, so we mock that
        val ctx = KContext(raw)
        val result = ctx.choice("mode", listOf("survival", "creative", "adventure"))
        assertEquals("creative", result)
    }

    @Test
    fun `choice extraction rejects invalid value`() {
        val raw = mockk<com.mojang.brigadier.context.CommandContext<VelocitySource>>()
        every { raw.source } returns mockk()
        every { raw.getArgument("mode", String::class.java) } returns "invalid"

        val ctx = KContext(raw)
        val ex = assertFailsWith<CommandSyntaxException> {
            ctx.choice("mode", listOf("survival", "creative"))
        }
        assertTrue("invalid" in ex.message!!)
        assertTrue("survival" in ex.message!!)
        assertTrue("creative" in ex.message!!)
    }

    @Test
    fun `enum registers word argument`() {
        val cmd = kraftlinCommand("test") {
            enum<GameMode>("mode") {
                executes { }
            }
        }

        val arg = cmd.node.getChild("mode")
        assertNotNull(arg, "enum argument should exist")
    }

    @Test
    fun `enum extraction returns valid value`() {
        val raw = mockk<com.mojang.brigadier.context.CommandContext<VelocitySource>>()
        every { raw.source } returns mockk()
        every { raw.getArgument("mode", String::class.java) } returns "creative"

        val ctx = KContext(raw)
        val result = ctx.enum<GameMode>("mode")
        assertEquals(GameMode.CREATIVE, result)
    }

    @Test
    fun `enum extraction rejects invalid value`() {
        val raw = mockk<com.mojang.brigadier.context.CommandContext<VelocitySource>>()
        every { raw.source } returns mockk()
        every { raw.getArgument("mode", String::class.java) } returns "invalid"

        val ctx = KContext(raw)
        val ex = assertFailsWith<CommandSyntaxException> {
            ctx.enum<GameMode>("mode")
        }
        assertTrue("invalid" in ex.message!!)
        assertTrue("survival" in ex.message!!)
    }

    @Test
    fun `enum uses lowercase tokens`() {
        val raw = mockk<com.mojang.brigadier.context.CommandContext<VelocitySource>>()
        every { raw.source } returns mockk()
        every { raw.getArgument("mode", String::class.java) } returns "survival"

        val ctx = KContext(raw)
        assertEquals(GameMode.SURVIVAL, ctx.enum<GameMode>("mode"))
    }
}
