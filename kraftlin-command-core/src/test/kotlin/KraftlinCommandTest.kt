package io.github.kraftlin.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KraftlinCommandTest {

    @Test
    fun `builds nested literals and executes`() {
        val dispatcher = CommandDispatcher<Any>()
        var executed = false
        val root = brigadierCommand<Any>("root") {
            literal("sub") {
                executes {
                    executed = true
                }
            }
        }
        dispatcher.root.addChild(root)

        dispatcher.execute("root sub", Any())
        assertTrue(executed)
    }

    @Test
    fun `test executesResult`() {
        val dispatcher = CommandDispatcher<Any>()
        val root = brigadierCommand<Any>("test") {
            executesResult { 42 }
        }
        dispatcher.root.addChild(root)

        val result = dispatcher.execute("test", Any())
        assertEquals(42, result)
    }

    @Test
    fun `test suggestsStatic`() {
        val dispatcher = CommandDispatcher<Any>()
        val root = brigadierCommand<Any>("test") {
            argument("arg", StringArgumentType.word()) {
                suggestsStatic("a", "b", "c")
            }
        }
        dispatcher.root.addChild(root)

        val parse = dispatcher.parse("test ", Any())
        val suggestions = dispatcher.getCompletionSuggestions(parse).get()
        val values = suggestions.list.map { it.text }
        assertEquals(listOf("a", "b", "c"), values)
    }

    @Test
    fun `test suggests`() {
        val dispatcher = CommandDispatcher<Any>()
        val root = brigadierCommand<Any>("test") {
            argument("arg", StringArgumentType.word()) {
                suggests { _, builder ->
                    builder.suggest("dynamic")
                    builder.buildFuture()
                }
            }
        }
        dispatcher.root.addChild(root)

        val parse = dispatcher.parse("test ", Any())
        val suggestions = dispatcher.getCompletionSuggestions(parse).get()
        val values = suggestions.list.map { it.text }
        assertEquals(listOf("dynamic"), values)
    }

    @Test
    fun `requirements are combined`() {
        val root = brigadierCommand<Int>("cmd") {
            requires { it > 0 }
            requires { it % 2 == 0 }
            executes { }
        }.createBuilder().build()

        // requirement is stored on the built node
        assertEquals(true, root.requirement.test(2))
        assertEquals(false, root.requirement.test(1))
        assertEquals(false, root.requirement.test(-2))
    }
}
