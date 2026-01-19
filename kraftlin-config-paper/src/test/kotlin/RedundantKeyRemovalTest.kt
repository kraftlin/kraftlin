package io.github.kraftlin.config.paper

import io.github.kraftlin.config.AbstractConfig
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class RedundantKeyRemovalTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun `test redundant keys are removed`() {
        val configFile = directory.resolve("config.yml")
        val initialContent = """
            active: value
            legacy: old-value
            section:
              active_in_section: value
              legacy_in_section: old-value
            legacy_section:
              key: value
            map:
              entry1: value1
              entry2: value2
        """.trimIndent()
        Files.writeString(configFile, initialContent)

        val config = object : AbstractConfig(wrapConfig(configFile)) {
            val active by config("active", "default")
            val activeInSection by config("section.active_in_section", "default")
            val myMap by config("map", mapOf("default" to "value"))
        }

        // We need to reload to ensure the wrapper has the content from disk
        config.reloadConfig()
        
        config.removeRedundantKeys()
        config.save()

        val content = Files.readString(configFile)
        
        assertTrue(content.contains("active: value"), "Active key should be preserved")
        assertTrue(content.contains("active_in_section: value"), "Active key in section should be preserved")
        assertTrue(content.contains("section:"), "Section containing active key should be preserved")
        
        assertTrue(content.contains("map:"), "Map section should be preserved")
        assertTrue(content.contains("entry1: value1"), "Map entries should be preserved")
        assertTrue(content.contains("entry2: value2"), "Map entries should be preserved")
        
        assertFalse(content.contains("legacy: old-value"), "Legacy key should be removed")
        assertFalse(content.contains("legacy_in_section: old-value"), "Legacy key in section should be removed")
        assertFalse(content.contains("legacy_section:"), "Legacy section should be removed")
    }
}
