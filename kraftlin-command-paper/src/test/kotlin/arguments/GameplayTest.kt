package io.github.kbrigx.paper.arguments

import com.mojang.brigadier.context.CommandContext
import io.github.kbrigx.core.KContext
import io.github.kbrigx.paper.PaperSource
import io.mockk.every
import io.mockk.mockk
import org.bukkit.GameMode
import org.bukkit.scoreboard.DisplaySlot
import kotlin.test.Test
import kotlin.test.assertEquals

class GameplayTest {

    @Test
    fun `test gameMode helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = GameMode.SURVIVAL
        every { raw.getArgument("arg", GameMode::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.gameMode("arg"))
    }

    // objectiveCriteria depends on the server being initialized, which means it is not straightforward to test.

    @Test
    fun `test scoreboardDisplaySlot helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = DisplaySlot.SIDEBAR
        every { raw.getArgument("arg", DisplaySlot::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.scoreboardDisplaySlot("arg"))
    }
}
