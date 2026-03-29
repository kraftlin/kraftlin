@file:Suppress("unused")

package io.github.kraftlin.config.paper

import io.github.kraftlin.config.AbstractConfig
import io.github.kraftlin.config.wrapConfig
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertTrue

internal class CommentApplicationTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun `test comments are applied when no comments exist`() {
        val configFile = directory.resolve("config_none.yml")
        // File exists with a value but no comments
        Files.writeString(configFile, "existing: value\n")

        val config = object : AbstractConfig(wrapConfig(configFile)) {
            val existing by config("existing", "default", "New Comment")
        }

        config.saveDefaults()

        val content = Files.readString(configFile)
        assertTrue(content.contains("# New Comment"), "Comment should be added to existing value with no comments")
    }

    @Test
    fun `test comments are NOT applied when comments already exist`() {
        val configFile = directory.resolve("config_existing.yml")
        // File exists with a value and an existing comment
        Files.writeString(configFile, "# Old Comment\nexisting: value\n")

        val config = object : AbstractConfig(wrapConfig(configFile)) {
            val existing by config("existing", "default", "New Comment")
        }

        // Reload to ensure the wrapper sees the existing comment
        config.reloadConfig()
        config.saveDefaults()

        val content = Files.readString(configFile)
        assertTrue(content.contains("# Old Comment"), "Existing comment should be preserved")
        assertTrue(!content.contains("# New Comment"), "New comment should not overwrite existing one")
    }

    @Test
    fun `test comments are applied to new default values`() {
        val configFile = directory.resolve("config_new.yml")
        // File does not exist or is empty

        val config = object : AbstractConfig(wrapConfig(configFile)) {
            val newValue by config("newValue", "defaultValue", "Comment for new value")
        }

        config.saveDefaults()

        val content = Files.readString(configFile)
        // Note: Paper might have specific behavior regarding where it places comments for new values
        // but it should be present in the file if copyDefaults(true) was used.
        assertTrue(content.contains("# Comment for new value"), "Comment should be added for new default value")
        assertTrue(content.contains("newValue: defaultValue"), "Default value should be present")
    }
}
