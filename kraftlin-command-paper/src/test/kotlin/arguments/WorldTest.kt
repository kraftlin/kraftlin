@file:Suppress("UnstableApiUsage")

package io.github.kbrigx.paper.arguments

import com.mojang.brigadier.context.CommandContext
import io.github.kbrigx.core.KContext
import io.github.kbrigx.paper.PaperSource
import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.command.brigadier.argument.predicate.BlockInWorldPredicate
import io.papermc.paper.command.brigadier.argument.predicate.ItemStackPredicate
import org.bukkit.HeightMap
import org.bukkit.World
import org.bukkit.block.BlockState
import org.bukkit.inventory.ItemStack
import kotlin.test.Test
import kotlin.test.assertEquals

class WorldTest {

    @Test
    fun `test world helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = mockk<World>()
        every { raw.getArgument("arg", World::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.world("arg"))
    }

    @Test
    fun `test blockState helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = mockk<BlockState>()
        every { raw.getArgument("arg", BlockState::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.blockState("arg"))
    }

    @Test
    fun `test itemStack helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = mockk<ItemStack>()
        every { raw.getArgument("arg", ItemStack::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.itemStack("arg"))
    }

    @Test
    fun `test blockInWorldPredicate helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = mockk<BlockInWorldPredicate>()
        every { raw.getArgument("arg", BlockInWorldPredicate::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.blockInWorldPredicate("arg"))
    }

    @Test
    fun `test itemPredicate helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = mockk<ItemStackPredicate>()
        every { raw.getArgument("arg", ItemStackPredicate::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.itemPredicate("arg"))
    }

    @Test
    fun `test heightMap helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = HeightMap.WORLD_SURFACE
        every { raw.getArgument("arg", HeightMap::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.heightMap("arg"))
    }

    @Test
    fun `test time helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = 1200
        every { raw.getArgument("arg", Int::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.time("arg"))
    }
}
