package io.github.kraftlin.command

import com.mojang.brigadier.CommandDispatcher
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for the generic [PlatformAdapter] execution and requirement helpers.
 *
 * Uses a simple `String` source type with a test adapter to verify the abstraction
 * works independently of any platform.
 */
class PlatformAdapterTest {

    data class TestSender(val name: String, val permissions: Set<String>)

    /** Maps a source string to a TestSender. */
    private val testAdapter = object : PlatformAdapter<String, TestSender> {
        override fun sender(source: String): TestSender =
            TestSender(source, setOf("test.allowed"))

        override fun hasPermission(source: String, permission: String): Boolean =
            permission in sender(source).permissions
    }

    @Test
    fun `executes with adapter provides sender`() {
        val dispatcher = CommandDispatcher<String>()
        var receivedSender: TestSender? = null

        val root = brigadierCommand<String>("cmd") {
            executes(testAdapter) { sender, _ ->
                receivedSender = sender
            }
        }
        dispatcher.root.addChild(root)
        dispatcher.execute("cmd", "alice")

        assertEquals(TestSender("alice", setOf("test.allowed")), receivedSender)
    }

    @Test
    fun `executesResult with adapter returns result`() {
        val dispatcher = CommandDispatcher<String>()

        val root = brigadierCommand<String>("cmd") {
            executesResult(testAdapter) { sender, _ ->
                sender.name.length
            }
        }
        dispatcher.root.addChild(root)

        assertEquals(5, dispatcher.execute("cmd", "alice"))
        assertEquals(3, dispatcher.execute("cmd", "bob"))
    }

    @Test
    fun `requiresPermission with adapter grants access`() {
        val root = brigadierCommand<String>("cmd") {
            requiresPermission(testAdapter, "test.allowed")
            executes { }
        }

        assertTrue(root.requirement.test("anyone"))
    }

    @Test
    fun `requiresPermission with adapter denies access`() {
        val root = brigadierCommand<String>("cmd") {
            requiresPermission(testAdapter, "test.denied")
            executes { }
        }

        assertFalse(root.requirement.test("anyone"))
    }

    @Test
    fun `requiresSender with adapter filters by predicate`() {
        val root = brigadierCommand<String>("cmd") {
            requiresSender(testAdapter) { it.name.startsWith("a") }
            executes { }
        }

        assertTrue(root.requirement.test("alice"))
        assertFalse(root.requirement.test("bob"))
    }

    @Test
    fun `adapter combines with regular requires`() {
        val root = brigadierCommand<String>("cmd") {
            requires { it.length > 3 }
            requiresPermission(testAdapter, "test.allowed")
            executes { }
        }

        assertTrue(root.requirement.test("alice"))
        assertFalse(root.requirement.test("bob"))  // too short
    }
}
