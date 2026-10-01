package io.github.kraftlin.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.CommandSyntaxException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

    @Test
    fun `alias of a literal without sub-commands executes`() {
        val dispatcher = CommandDispatcher<Any>()
        var runs = 0
        dispatcher.root.addChild(brigadierCommand<Any>("test") {
            literal("info", "help", "i") {
                executes { runs++ }
            }
        })

        dispatcher.execute("test info", Any())
        dispatcher.execute("test help", Any())
        dispatcher.execute("test i", Any())

        assertEquals(3, runs)
    }

    @Test
    fun `alias reaches the sub-commands of its literal`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: String? = null
        dispatcher.root.addChild(brigadierCommand<Any>("test") {
            literal("config", "c") {
                word("key") {
                    executes { ctx -> result = ctx.word("key") }
                }
            }
        })

        dispatcher.execute("test c speed", Any())

        assertEquals("speed", result)
    }

    @Test
    fun `alias keeps the requirement of its literal`() {
        val dispatcher = CommandDispatcher<Any>()
        var runs = 0
        dispatcher.root.addChild(brigadierCommand<Any>("test") {
            literal("admin", "a") {
                requires { false }
                executes { runs++ }
                literal("reset") {
                    executes { runs++ }
                }
            }
        })

        for (input in listOf("test admin", "test a", "test admin reset", "test a reset")) {
            assertFailsWith<CommandSyntaxException>(input) { dispatcher.execute(input, Any()) }
        }
        assertEquals(0, runs)
    }

    @Test
    fun `aliases are suggested like their literal`() {
        val dispatcher = CommandDispatcher<Any>()
        dispatcher.root.addChild(brigadierCommand<Any>("test") {
            literal("info", "help") {
                executes { }
            }
        })

        val suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("test ", Any())).get()

        assertEquals(listOf("help", "info"), suggestions.list.map { it.text }.sorted())
    }
}
