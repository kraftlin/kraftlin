package io.github.kraftlin.command.velocity

import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import io.github.kraftlin.command.KContext
import io.github.kraftlin.command.executes
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VelocityCoreTest {

    @Test
    fun `test sender helper`() {
        val source = mockk<CommandSource>()
        val raw = mockk<com.mojang.brigadier.context.CommandContext<VelocitySource>>()
        every { raw.source } returns source

        val ctx = KContext(raw)
        assertEquals(source, ctx.sender)
    }

    @Test
    fun `test requirePlayer success`() {
        val player = mockk<Player>()
        val raw = mockk<com.mojang.brigadier.context.CommandContext<VelocitySource>>()
        every { raw.source } returns player

        val ctx = KContext(raw)
        assertEquals(player, ctx.requirePlayer())
    }

    @Test
    fun `test requirePlayer failure`() {
        val source = mockk<CommandSource>()
        val raw = mockk<com.mojang.brigadier.context.CommandContext<VelocitySource>>()
        every { raw.source } returns source

        val ctx = KContext(raw)
        assertFailsWith<CommandSyntaxException> {
            ctx.requirePlayer()
        }
    }

    @Test
    fun `test requiresPermission`() {
        val source = mockk<CommandSource>()
        every { source.hasPermission("test.perm") } returns true
        every { source.hasPermission("fail.perm") } returns false

        val node = kraftlinCommand("test") {
            requiresPermission("test.perm")
            executes { }
        }.node

        assertTrue(node.requirement.test(source))

        val failNode = kraftlinCommand("fail") {
            requiresPermission("fail.perm")
            executes { }
        }.node

        assertFalse(failNode.requirement.test(source))
    }

    @Test
    fun `test requiresPlayer`() {
        val player = mockk<Player>()
        val source = mockk<CommandSource>()

        val node = kraftlinCommand("test") {
            requiresPlayer()
            executes { }
        }.node

        assertTrue(node.requirement.test(player))
        assertFalse(node.requirement.test(source))
    }

    @Test
    fun `test requiresSender`() {
        val source = mockk<CommandSource>()

        val node = kraftlinCommand("test") {
            requiresSender { it == source }
            executes { }
        }.node

        assertTrue(node.requirement.test(source))
        assertFalse(node.requirement.test(mockk<CommandSource>()))
    }

    @Test
    fun `test velocity command entry point`() {
        val cmd = kraftlinCommand("test") {
            executes { }
        }
        assertEquals("test", cmd.node.literal)
    }

    @Test
    fun `test command metadata`() {
        val cmd = kraftlinCommand(
            name = "test",
            description = "A test command",
            aliases = listOf("t", "tst"),
        ) {
            executes { }
        }

        assertEquals("test", cmd.node.literal)
        assertEquals("A test command", cmd.description)
        assertEquals(listOf("t", "tst"), cmd.aliases)
    }
}
