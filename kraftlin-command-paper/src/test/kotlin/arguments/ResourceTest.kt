package io.github.kbrigx.paper.arguments

import com.mojang.brigadier.context.CommandContext
import io.github.kbrigx.core.KContext
import io.github.kbrigx.paper.PaperSource
import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import kotlin.test.Test
import kotlin.test.assertEquals

class ResourceTest {

    @Test
    fun `test key helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = Key.key("minecraft:stone")
        every { raw.getArgument("arg", Key::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.key("arg"))
    }

    @Test
    fun `test namespacedKey helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = NamespacedKey.minecraft("stone")
        every { raw.getArgument("arg", NamespacedKey::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.namespacedKey("arg"))
    }

    @Test
    fun `test resource helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = mockk<Any>()
        every { raw.getArgument("arg", Any::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.resource<Any>("arg"))
    }

    @Test
    fun `test resourceKey helper`() {
        val raw = mockk<CommandContext<PaperSource>>()
        val value = mockk<TypedKey<Any>>()
        every { raw.getArgument("arg", TypedKey::class.java) } returns value

        val ctx = KContext(raw)
        assertEquals(value, ctx.resourceKey<Any>("arg"))
    }
}
