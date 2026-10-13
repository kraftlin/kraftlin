package io.github.kraftlin.config.paper

import io.github.kraftlin.config.wrapConfig
import org.bukkit.Material
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class MaterialConfigTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun `empty material list reads as empty set`() {
        val configFile = directory.resolve("config.yml")
        Files.writeString(configFile, "blocks: []\n")

        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {
            val blocks: Set<Material> by config("blocks", listOf(Material.STONE))
        }

        assertEquals(emptySet(), config.blocks)
    }

    @Test
    fun `unknown material names are skipped with a warning`() {
        val configFile = directory.resolve("config.yml")
        Files.writeString(configFile, "blocks:\n- stone\n- not_a_material\n")
        val warnings = RecordingHandler()

        val config = object : AbstractBukkitConfig(wrapConfig(configFile, loggerWith(warnings))) {
            val blocks: Set<Material> by config("blocks", listOf(Material.DIRT))
        }

        assertEquals(setOf(Material.STONE), config.blocks)
        assertEquals(1, warnings.messages.size)
        assertTrue(warnings.messages.single().contains("blocks"), "Warning should name the path")
        assertTrue(warnings.messages.single().contains("not_a_material"), "Warning should name the unknown entry")
    }

    @Test
    fun `only unknown material names read as empty set`() {
        val configFile = directory.resolve("config.yml")
        Files.writeString(configFile, "blocks:\n- not_a_material\n")

        val config = object : AbstractBukkitConfig(wrapConfig(configFile, loggerWith(RecordingHandler()))) {
            val blocks: Set<Material> by config("blocks", listOf(Material.STONE))
        }

        assertEquals(emptySet(), config.blocks)
    }

    @Test
    fun `material list comments are written`() {
        val configFile = directory.resolve("config.yml")

        val config = object : AbstractBukkitConfig(wrapConfig(configFile)) {
            val blocks: Set<Material> by config("blocks", listOf(Material.STONE), "Blocks the pet may dig")
        }

        config.saveDefaults()

        val content = Files.readString(configFile)
        assertTrue(content.contains("# Blocks the pet may dig"), "Comment should be written for the material list")
        assertEquals(setOf(Material.STONE), config.blocks)
    }

    private fun loggerWith(handler: Handler): Logger =
        Logger.getLogger("kraftlin-test-${System.nanoTime()}").apply {
            useParentHandlers = false
            addHandler(handler)
        }

    private class RecordingHandler : Handler() {
        val messages = mutableListOf<String>()

        override fun publish(record: LogRecord) {
            if (record.level == Level.WARNING) messages += record.message
        }

        override fun flush() {}

        override fun close() {}
    }
}
