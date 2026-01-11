package de.devsylum.kconfig

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class DatabaseConfigTest {

    @get:Rule
    val testDirectory = TemporaryFolder()

    @Test
    fun `load properties from file`() {
        val dbConfig = testDirectory.newFile("database.properties").toPath()
        Files.newBufferedWriter(dbConfig, Charsets.UTF_8).use { writer ->
            writer.write("url=jdbc:mysql://devsylum.de:3306/database")
            writer.newLine()
            writer.write("user=testuser")
            writer.newLine()
            writer.write("password=testpassword")
            writer.newLine()
            writer.flush()
        }

        val expected = SqlConfiguration("jdbc:mysql://devsylum.de:3306/database", "testuser", "testpassword")
        val actual = loadSqlConfiguration(testDirectory.root.toPath())
        assertEquals(expected, actual)
    }

    @Test
    fun `store and load default configuration`() {
        val expected = SqlConfiguration("jdbc:mysql://localhost:3306/dbname", "exampleuser", "examplepassword")
        val actual = loadSqlConfiguration(testDirectory.root.toPath())
        assertEquals(expected, actual)
    }

    @Test
    fun `prevent password leakage through toString method`() {
        val config = SqlConfiguration("", "", "secret")
        assertFalse(config.toString().contains("secret"))
    }
}
