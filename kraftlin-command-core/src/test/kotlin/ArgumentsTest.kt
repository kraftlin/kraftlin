package io.github.kbrigx.core

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.exceptions.CommandSyntaxException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArgumentsTest {

    private fun <S> execute(dispatcher: CommandDispatcher<S>, input: String, source: S): Int {
        return dispatcher.execute(input, source)
    }

    @Test
    fun `test word argument`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: String? = null
        val cmd = KBrigx.command<Any>("test") {
            word("arg") {
                executes { ctx ->
                    result = ctx.word("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test hello", Any())
        assertEquals("hello", result)
    }

    @Test
    fun `test string argument`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: String? = null
        val cmd = KBrigx.command<Any>("test") {
            string("arg") {
                executes { ctx ->
                    result = ctx.string("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test \"hello world\"", Any())
        assertEquals("hello world", result)
    }

    @Test
    fun `test greedyString argument`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: String? = null
        val cmd = KBrigx.command<Any>("test") {
            greedyString("arg") {
                executes { ctx ->
                    result = ctx.greedyString("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test hello world and more", Any())
        assertEquals("hello world and more", result)
    }

    @Test
    fun `test integer argument`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Int? = null
        val cmd = KBrigx.command<Any>("test") {
            integer("arg") {
                executes { ctx ->
                    result = ctx.integer("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 123", Any())
        assertEquals(123, result)
    }

    @Test
    fun `test integer argument with min`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Int? = null
        val cmd = KBrigx.command<Any>("test") {
            integer("arg", 10) {
                executes { ctx ->
                    result = ctx.integer("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 15", Any())
        assertEquals(15, result)
    }

    @Test
    fun `test integer argument with min and max`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Int? = null
        val cmd = KBrigx.command<Any>("test") {
            integer("arg", 10, 20) {
                executes { ctx ->
                    result = ctx.integer("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 15", Any())
        assertEquals(15, result)
    }

    @Test
    fun `test boolean argument`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Boolean? = null
        val cmd = KBrigx.command<Any>("test") {
            boolean("arg") {
                executes { ctx ->
                    result = ctx.boolean("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test true", Any())
        assertEquals(true, result)
    }

    @Test
    fun `test double argument`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Double? = null
        val cmd = KBrigx.command<Any>("test") {
            double("arg") {
                executes { ctx ->
                    result = ctx.double("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 12.34", Any())
        assertEquals(12.34, result)
    }

    @Test
    fun `test double argument with min`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Double? = null
        val cmd = KBrigx.command<Any>("test") {
            double("arg", 10.0) {
                executes { ctx ->
                    result = ctx.double("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 15.0", Any())
        assertEquals(15.0, result)
    }

    @Test
    fun `test double argument with min and max`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Double? = null
        val cmd = KBrigx.command<Any>("test") {
            double("arg", 10.0, 20.0) {
                executes { ctx ->
                    result = ctx.double("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 15.0", Any())
        assertEquals(15.0, result)
    }

    @Test
    fun `test long argument`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Long? = null
        val cmd = KBrigx.command<Any>("test") {
            long("arg") {
                executes { ctx ->
                    result = ctx.long("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 1234567890", Any())
        assertEquals(1234567890L, result)
    }

    @Test
    fun `test long argument with min`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Long? = null
        val cmd = KBrigx.command<Any>("test") {
            long("arg", 10L) {
                executes { ctx ->
                    result = ctx.long("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 15", Any())
        assertEquals(15L, result)
    }

    @Test
    fun `test long argument with min and max`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Long? = null
        val cmd = KBrigx.command<Any>("test") {
            long("arg", 10L, 20L) {
                executes { ctx ->
                    result = ctx.long("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 15", Any())
        assertEquals(15L, result)
    }

    @Test
    fun `test float argument`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Float? = null
        val cmd = KBrigx.command<Any>("test") {
            float("arg") {
                executes { ctx ->
                    result = ctx.float("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 12.34", Any())
        assertEquals(12.34f, result)
    }

    @Test
    fun `test float argument with min`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Float? = null
        val cmd = KBrigx.command<Any>("test") {
            float("arg", 10.0f) {
                executes { ctx ->
                    result = ctx.float("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 15.0", Any())
        assertEquals(15.0f, result)
    }

    @Test
    fun `test float argument with min and max`() {
        val dispatcher = CommandDispatcher<Any>()
        var result: Float? = null
        val cmd = KBrigx.command<Any>("test") {
            float("arg", 10.0f, 20.0f) {
                executes { ctx ->
                    result = ctx.float("arg")
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 15.0", Any())
        assertEquals(15.0f, result)
    }

    @Test
    fun `test generic arg method`() {
        val dispatcher = CommandDispatcher<Any>()
        var intResult: Int? = null
        var stringResult: String? = null
        val cmd = KBrigx.command<Any>("test") {
            integer("int") {
                word("string") {
                    executes { ctx ->
                        intResult = ctx.integer("int")
                        stringResult = ctx.string("string")
                    }
                }
            }
        }
        dispatcher.root.addChild(cmd)

        execute(dispatcher, "test 123 hello", Any())
        assertEquals(123, intResult)
        assertEquals("hello", stringResult)
    }

    @Test
    fun `test integer argument out of range`() {
        val dispatcher = CommandDispatcher<Any>()
        val cmd = KBrigx.command<Any>("test") {
            integer("arg", 10, 20) {
                executes { }
            }
        }
        dispatcher.root.addChild(cmd)

        assertFailsWith<CommandSyntaxException> {
            execute(dispatcher, "test 9", Any())
        }
        assertFailsWith<CommandSyntaxException> {
            execute(dispatcher, "test 21", Any())
        }
    }

    @Test
    fun `test integer argument wrong type`() {
        val dispatcher = CommandDispatcher<Any>()
        val cmd = KBrigx.command<Any>("test") {
            integer("arg") {
                executes { }
            }
        }
        dispatcher.root.addChild(cmd)

        assertFailsWith<CommandSyntaxException> {
            execute(dispatcher, "test hello", Any())
        }
    }

    @Test
    fun `test long argument out of range`() {
        val dispatcher = CommandDispatcher<Any>()
        val cmd = KBrigx.command<Any>("test") {
            long("arg", 10L, 20L) {
                executes { }
            }
        }
        dispatcher.root.addChild(cmd)

        assertFailsWith<CommandSyntaxException> {
            execute(dispatcher, "test 9", Any())
        }
        assertFailsWith<CommandSyntaxException> {
            execute(dispatcher, "test 21", Any())
        }
    }

    @Test
    fun `test double argument out of range`() {
        val dispatcher = CommandDispatcher<Any>()
        val cmd = KBrigx.command<Any>("test") {
            double("arg", 10.0, 20.0) {
                executes { }
            }
        }
        dispatcher.root.addChild(cmd)

        assertFailsWith<CommandSyntaxException> {
            execute(dispatcher, "test 9.9", Any())
        }
        assertFailsWith<CommandSyntaxException> {
            execute(dispatcher, "test 20.1", Any())
        }
    }

    @Test
    fun `test float argument out of range`() {
        val dispatcher = CommandDispatcher<Any>()
        val cmd = KBrigx.command<Any>("test") {
            float("arg", 10.0f, 20.0f) {
                executes { }
            }
        }
        dispatcher.root.addChild(cmd)

        assertFailsWith<CommandSyntaxException> {
            execute(dispatcher, "test 9.9", Any())
        }
        assertFailsWith<CommandSyntaxException> {
            execute(dispatcher, "test 20.1", Any())
        }
    }

    @Test
    fun `test boolean argument wrong type`() {
        val dispatcher = CommandDispatcher<Any>()
        val cmd = KBrigx.command<Any>("test") {
            boolean("arg") {
                executes { }
            }
        }
        dispatcher.root.addChild(cmd)

        assertFailsWith<CommandSyntaxException> {
            execute(dispatcher, "test notabool", Any())
        }
    }
}
