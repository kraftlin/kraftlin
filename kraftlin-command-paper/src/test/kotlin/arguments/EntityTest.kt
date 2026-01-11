package io.github.kbrigx.paper.arguments

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import io.github.kbrigx.core.KContext
import io.github.kbrigx.paper.PaperSource
import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver
import org.bukkit.entity.Entity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EntityTest {

    @Test
    fun `test entities helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<EntitySelectorArgumentResolver>()
        val entity = mockk<Entity>()
        val entities = listOf(entity)

        every { raw.source } returns source
        every { raw.getArgument("arg", EntitySelectorArgumentResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns entities

        val ctx = KContext(raw)
        assertEquals(entities, ctx.entities("arg"))
    }

    @Test
    fun `test entity helper success`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<EntitySelectorArgumentResolver>()
        val entity = mockk<Entity>()

        every { raw.source } returns source
        every { raw.getArgument("arg", EntitySelectorArgumentResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns listOf(entity)

        val ctx = KContext(raw)
        assertEquals(entity, ctx.entity("arg"))
    }

    @Test
    fun `test entity helper not found`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<EntitySelectorArgumentResolver>()

        every { raw.source } returns source
        every { raw.getArgument("arg", EntitySelectorArgumentResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns emptyList()

        val ctx = KContext(raw)
        assertFailsWith<CommandSyntaxException> {
            ctx.entity("arg")
        }
    }

    @Test
    fun `test entity helper too many`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val source = mockk<PaperSource>()
        val resolver = mockk<EntitySelectorArgumentResolver>()
        val entity1 = mockk<Entity>()
        val entity2 = mockk<Entity>()

        every { raw.source } returns source
        every { raw.getArgument("arg", EntitySelectorArgumentResolver::class.java) } returns resolver
        every { resolver.resolve(source) } returns listOf(entity1, entity2)

        val ctx = KContext(raw)
        assertFailsWith<CommandSyntaxException> {
            ctx.entity("arg")
        }
    }
}
