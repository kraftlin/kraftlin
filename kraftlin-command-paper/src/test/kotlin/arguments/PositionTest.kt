@file:Suppress("UnstableApiUsage")

package io.github.kbrigx.paper.arguments

import com.mojang.brigadier.context.CommandContext
import io.github.kbrigx.core.KContext
import io.github.kbrigx.paper.PaperSource
import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.command.brigadier.argument.position.ColumnBlockPosition
import io.papermc.paper.command.brigadier.argument.position.ColumnFinePosition
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnBlockPositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.ColumnFinePositionResolver
import io.papermc.paper.command.brigadier.argument.resolvers.FinePositionResolver
import io.papermc.paper.math.BlockPosition
import io.papermc.paper.math.FinePosition
import kotlin.test.Test
import kotlin.test.assertEquals

class PositionTest {

    @Test
    fun `test blockPosition helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<BlockPositionResolver>()
        val value = mockk<BlockPosition>()

        every { raw.source } returns source
        every { raw.getArgument("arg", BlockPositionResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.blockPosition("arg"))
    }

    @Test
    fun `test columnBlockPosition helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<ColumnBlockPositionResolver>()
        val value = mockk<ColumnBlockPosition>()

        every { raw.source } returns source
        every { raw.getArgument("arg", ColumnBlockPositionResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.columnBlockPosition("arg"))
    }

    @Test
    fun `test finePosition helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<FinePositionResolver>()
        val value = mockk<FinePosition>()

        every { raw.source } returns source
        every { raw.getArgument("arg", FinePositionResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.finePosition("arg"))
    }

    @Test
    fun `test columnFinePosition helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<ColumnFinePositionResolver>()
        val value = mockk<ColumnFinePosition>()

        every { raw.source } returns source
        every { raw.getArgument("arg", ColumnFinePositionResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.columnFinePosition("arg"))
    }
}
