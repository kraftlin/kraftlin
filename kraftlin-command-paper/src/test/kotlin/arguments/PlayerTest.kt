package io.github.kbrigx.paper.arguments

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.github.kbrigx.core.KContext
import io.github.kbrigx.paper.PaperSource
import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlayerTest {

    @Test
    fun `test players helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<PlayerSelectorArgumentResolver>()
        val player = mockk<Player>()
        val players = listOf(player)

        every { raw.source } returns source
        every { raw.getArgument("arg", PlayerSelectorArgumentResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns players

        val ctx = KContext(raw)
        assertEquals(players, ctx.players("arg"))
    }

    @Test
    fun `test player helper success`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<PlayerSelectorArgumentResolver>()
        val player = mockk<Player>()

        every { raw.source } returns source
        every { raw.getArgument("arg", PlayerSelectorArgumentResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns listOf(player)

        val ctx = KContext(raw)
        assertEquals(player, ctx.player("arg"))
    }

    @Test
    fun `test player helper not found`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<PlayerSelectorArgumentResolver>()

        every { raw.source } returns source
        every { raw.getArgument("arg", PlayerSelectorArgumentResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns emptyList()

        val ctx = KContext(raw)
        assertFailsWith<CommandSyntaxException> {
            ctx.player("arg")
        }
    }

    @Test
    fun `test player helper too many`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<PlayerSelectorArgumentResolver>()
        val player1 = mockk<Player>()
        val player2 = mockk<Player>()

        every { raw.source } returns source
        every { raw.getArgument("arg", PlayerSelectorArgumentResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns listOf(player1, player2)

        val ctx = KContext(raw)
        assertFailsWith<CommandSyntaxException> {
            ctx.player("arg")
        }
    }
}
