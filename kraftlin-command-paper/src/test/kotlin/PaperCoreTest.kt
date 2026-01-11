package io.github.kraftlin.command.paper

import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.github.kraftlin.command.KContext
import io.github.kraftlin.command.executes
import io.mockk.every
import io.mockk.mockk
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PaperCoreTest {

    @Test
    fun `test sender helper`() {
        val sender = mockk<CommandSender>()
        val source = mockk<PaperSource>()
        every { source.sender } returns sender
        val raw = mockk<com.mojang.brigadier.context.CommandContext<PaperSource>>()
        every { raw.source } returns source

        val ctx = KContext(raw)
        assertEquals(sender, ctx.sender)
    }

    @Test
    fun `test requirePlayer helper success`() {
        val player = mockk<Player>()
        val source = mockk<PaperSource>()
        every { source.sender } returns player
        val raw = mockk<com.mojang.brigadier.context.CommandContext<PaperSource>>()
        every { raw.source } returns source

        val ctx = KContext(raw)
        assertEquals(player, ctx.requirePlayer())
    }

    @Test
    fun `test requirePlayer helper failure`() {
        val sender = mockk<CommandSender>()
        val source = mockk<PaperSource>()
        every { source.sender } returns sender
        val raw = mockk<com.mojang.brigadier.context.CommandContext<PaperSource>>()
        every { raw.source } returns source

        val ctx = KContext(raw)
        assertFailsWith<CommandSyntaxException> {
            ctx.requirePlayer()
        }
    }

    @Test
    fun `test requiresPermission`() {
        val sender = mockk<CommandSender>()
        val source = mockk<PaperSource>()
        every { source.sender } returns sender
        every { sender.hasPermission("test.perm") } returns true
        every { sender.hasPermission("fail.perm") } returns false

        val node = kraftlinCommand("test") {
            requiresPermission("test.perm")
            executes { }
        }.createBuilder().build()

        assertEquals(true, node.requirement.test(source))

        val failNode = kraftlinCommand("fail") {
            requiresPermission("fail.perm")
            executes { }
        }.createBuilder().build()

        assertEquals(false, failNode.requirement.test(source))
    }

    @Test
    fun `test requiresPlayer`() {
        val player = mockk<Player>()
        val sender = mockk<CommandSender>()

        val playerSource = mockk<PaperSource>()
        every { playerSource.sender } returns player

        val senderSource = mockk<PaperSource>()
        every { senderSource.sender } returns sender

        val node = kraftlinCommand("test") {
            requiresPlayer()
            executes { }
        }.createBuilder().build()

        assertEquals(true, node.requirement.test(playerSource))
        assertEquals(false, node.requirement.test(senderSource))
    }

    @Test
    fun `test requiresSender`() {
        val sender = mockk<CommandSender>()
        val source = mockk<PaperSource>()
        every { source.sender } returns sender

        val node = kraftlinCommand("test") {
            requiresSender { it == sender }
            executes { }
        }.createBuilder().build()

        assertEquals(true, node.requirement.test(source))

        val otherSource = mockk<PaperSource>()
        every { otherSource.sender } returns mockk<CommandSender>()
        assertEquals(false, node.requirement.test(otherSource))
    }

    @Test
    fun `test paper command entry point`() {
        val node = kraftlinCommand("test") {
            executes { }
        }
        assertEquals("test", node.literal)
    }
}
