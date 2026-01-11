package io.github.kbrigx.paper.arguments

import com.destroystokyo.paper.profile.PlayerProfile
import com.mojang.brigadier.context.CommandContext
import io.github.kbrigx.core.KContext
import io.github.kbrigx.paper.PaperSource
import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileTest {

    @Test
    fun `test playerProfiles helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<PlayerProfileListResolver>()
        val profile = mockk<PlayerProfile>()
        val profiles = listOf(profile)

        every { raw.source } returns source
        every { raw.getArgument("arg", PlayerProfileListResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns profiles

        val ctx = KContext(raw)
        assertEquals(profiles, ctx.playerProfiles("arg"))
    }

    @Test
    fun `test uuid helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = UUID.randomUUID()
        every { raw.getArgument("arg", UUID::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.uuid("arg"))
    }
}
