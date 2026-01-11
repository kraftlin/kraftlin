@file:Suppress("UnstableApiUsage")

package io.github.kbrigx.paper.arguments

import com.mojang.brigadier.context.CommandContext
import io.github.kbrigx.core.KContext
import io.github.kbrigx.paper.PaperSource
import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.command.brigadier.argument.AxisSet
import io.papermc.paper.command.brigadier.argument.resolvers.AngleResolver
import io.papermc.paper.command.brigadier.argument.resolvers.RotationResolver
import io.papermc.paper.entity.LookAnchor
import io.papermc.paper.math.Rotation
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import kotlin.test.Test
import kotlin.test.assertEquals

class RotationTest {

    @Test
    fun `test rotation helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<RotationResolver>()
        val value = mockk<Rotation>()

        every { raw.source } returns source
        every { raw.getArgument("arg", RotationResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.rotation("arg"))
    }

    @Test
    fun `test angle helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<AngleResolver>()
        val value = 45.0f

        every { raw.source } returns source
        every { raw.getArgument("arg", AngleResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.angle("arg"))
    }

    @Test
    fun `test axes helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = mockk<AxisSet>()
        every { raw.getArgument("arg", AxisSet::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.axes("arg"))
    }

    @Test
    fun `test templateMirror helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = Mirror.LEFT_RIGHT
        every { raw.getArgument("arg", Mirror::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.templateMirror("arg"))
    }

    @Test
    fun `test templateRotation helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = StructureRotation.CLOCKWISE_90
        every { raw.getArgument("arg", StructureRotation::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.templateRotation("arg"))
    }

    @Test
    fun `test entityAnchor helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = LookAnchor.EYES
        every { raw.getArgument("arg", LookAnchor::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.entityAnchor("arg"))
    }
}
