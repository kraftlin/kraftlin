package io.github.kbrigx.paper.arguments

import com.mojang.brigadier.context.CommandContext
import io.github.kbrigx.core.KContext
import io.github.kbrigx.paper.PaperSource
import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.command.brigadier.argument.SignedMessageResolver
import net.kyori.adventure.chat.SignedMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageTest {

    @Test
    fun `test signedMessage helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val resolver = mockk<SignedMessageResolver>()
        val message = mockk<SignedMessage>()
        val future = CompletableFuture.completedFuture(message)

        every { raw.getArgument("arg", SignedMessageResolver::class.java) } returns resolver
        every { resolver.resolveSignedMessage("arg", raw) } returns future

        val ctx = KContext(raw)
        assertEquals(future, ctx.signedMessage("arg"))
    }

    @Test
    fun `test component helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = Component.text("hello")
        every { raw.getArgument("arg", Component::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.component("arg"))
    }

    @Test
    fun `test style helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = Style.style(NamedTextColor.RED)
        every { raw.getArgument("arg", Style::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.style("arg"))
    }

    @Test
    fun `test namedColor helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = NamedTextColor.RED
        every { raw.getArgument("arg", NamedTextColor::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.namedColor("arg"))
    }

    @Test
    fun `test hexColor helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = TextColor.color(0xFF0000)
        every { raw.getArgument("arg", TextColor::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.hexColor("arg"))
    }
}
