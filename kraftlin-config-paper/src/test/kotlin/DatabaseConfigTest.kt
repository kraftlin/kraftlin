package io.github.kraftlin.config.paper

import io.github.kraftlin.config.SqlConfiguration
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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
        assertEquals(true, Files.exists(migrated))
    }

    @Test
    fun `prevent password leakage through toString method`() {
        val config = SqlConfiguration("", "", "secret")
        assertFalse(config.toString().contains("secret"))
    }
}
