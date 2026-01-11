package io.github.kbrigx.paper.arguments

import com.google.common.collect.Range
import com.mojang.brigadier.context.CommandContext
import io.github.kbrigx.core.KContext
import io.github.kbrigx.paper.PaperSource
import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.command.brigadier.argument.range.DoubleRangeProvider
import io.papermc.paper.command.brigadier.argument.range.IntegerRangeProvider
import kotlin.test.Test
import kotlin.test.assertEquals

class RangeTest {

    @Test
    fun `test integerRange helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val provider = mockk<IntegerRangeProvider>()
        val range = Range.closed(5, 10)

        every { raw.getArgument("arg", IntegerRangeProvider::class.java) } returns provider
        every { provider.range() } returns range

        val ctx = KContext(raw)
        assertEquals(range, ctx.integerRange("arg"))
    }

    @Test
    fun `test doubleRange helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val provider = mockk<DoubleRangeProvider>()
        val range = Range.closed(5.5, 10.5)

        every { raw.getArgument("arg", DoubleRangeProvider::class.java) } returns provider
        every { provider.range() } returns range

        val ctx = KContext(raw)
        assertEquals(range, ctx.doubleRange("arg"))
    }
}
