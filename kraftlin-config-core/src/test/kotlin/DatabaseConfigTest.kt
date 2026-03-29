package io.github.kraftlin.config

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DatabaseConfigTest {

    @TempDir
    lateinit var testDirectory: Path

    @Test
    fun `load yaml from file`() {
        val dbConfig = testDirectory.resolve("database.yml")
        Files.newBufferedWriter(dbConfig, Charsets.UTF_8).use { writer ->
            writer.write("url: 'jdbc:mysql://database.test:3306/database'")
            writer.newLine()
            writer.write("user: 'testuser'")
            writer.newLine()
            writer.write("password: 'testpassword'")
            writer.newLine()
            writer.flush()
        }

        val expected = SqlConfiguration("jdbc:mysql://database.test:3306/database", "testuser", "testpassword")
        val actual = loadSqlConfiguration(testDirectory)
        assertEquals(expected, actual)
    }

    @Test
    fun `store and load default configuration`() {
        val expected = SqlConfiguration("jdbc:mysql://localhost:3306/dbname", "exampleuser", "examplepassword")
        val actual = loadSqlConfiguration(testDirectory)
        assertEquals(expected, actual)
    }

    @Test
    fun `migrate legacy properties file to yaml`() {
        val legacyConfig = testDirectory.resolve("database.properties")
        Files.newBufferedWriter(legacyConfig, Charsets.UTF_8).use { writer ->
            writer.write("url=jdbc:mysql://database.test:3306/database")
            writer.newLine()
            writer.write("user=testuser")
            writer.newLine()
            writer.write("password=testpassword")
            writer.newLine()
            writer.flush()
        }

        val expected = SqlConfiguration("jdbc:mysql://database.test:3306/database", "testuser", "testpassword")
        val actual = loadSqlConfiguration(testDirectory)
        val migrated = testDirectory.resolve("database.yml")

        assertEquals(expected, actual)
        assertTrue(Files.exists(migrated))
    }

    @Test
    fun `prevent password leakage through toString method`() {
        val config = SqlConfiguration("", "", "secret")
        assertFalse(config.toString().contains("secret"))
    }

    // ========================= Error handling =========================

    @Test
    fun `malformed YAML throws ConfigException`() {
        val dbConfig = testDirectory.resolve("database.yml")
        Files.writeString(dbConfig, ":{invalid yaml")

        val exception = assertFailsWith<ConfigException> {
            loadSqlConfiguration(testDirectory, saveDefault = false)
        }
        assertTrue(exception.message!!.contains(dbConfig.toString()))
    }

    @Test
    fun `non-mapping YAML throws ConfigException`() {
        val dbConfig = testDirectory.resolve("database.yml")
        Files.writeString(dbConfig, "- item1\n- item2\n")

        val exception = assertFailsWith<ConfigException> {
            loadSqlConfiguration(testDirectory, saveDefault = false)
        }
        assertTrue(exception.message!!.contains("not a valid YAML mapping"))
    }

    @Test
    fun `missing required key throws ConfigException`() {
        val dbConfig = testDirectory.resolve("database.yml")
        Files.writeString(dbConfig, "url: 'jdbc:mysql://localhost:3306/db'\nuser: 'admin'\n")

        val exception = assertFailsWith<ConfigException> {
            loadSqlConfiguration(testDirectory, saveDefault = false)
        }
        assertTrue(exception.message!!.contains("password"), "Message should mention the missing key")
    }
}
