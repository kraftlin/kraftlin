package io.github.kraftlin.config

import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
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
        assertFalse(Files.exists(legacyConfig), "Legacy file should be removed after migration")
    }

    @Test
    fun `keep legacy properties file when yaml already exists`() {
        val legacyConfig = testDirectory.resolve("database.properties")
        Files.writeString(legacyConfig, "url=jdbc:mysql://legacy.test:3306/database\nuser=legacy\npassword=legacy\n")
        Files.writeString(
            testDirectory.resolve("database.yml"),
            "url: 'jdbc:mysql://database.test:3306/database'\nuser: 'testuser'\npassword: 'testpassword'\n"
        )

        val expected = SqlConfiguration("jdbc:mysql://database.test:3306/database", "testuser", "testpassword")
        val actual = loadSqlConfiguration(testDirectory)

        assertEquals(expected, actual)
        assertTrue(Files.exists(legacyConfig), "Legacy file must not be touched when it was not migrated")
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

    // ========================= Raw values =========================

    @Test
    fun `values that look like numbers or booleans are read exactly as typed`() {
        for (value in listOf("007007", "0123", "12.50", "1e3", "0x1F", "True")) {
            writeYaml("url: 'jdbc:mysql://localhost:3306/db'\nuser: $value\npassword: $value\n")

            val actual = loadSqlConfiguration(testDirectory, saveDefault = false)

            assertEquals(value, actual.user, "user: $value")
            assertEquals(value, actual.password, "password: $value")
        }
    }

    @Test
    fun `user and password set to YAML null read as empty`() {
        for (value in listOf("~", "null", "Null", "NULL")) {
            writeYaml("url: 'jdbc:mysql://localhost:3306/db'\nuser: $value\npassword: $value\n")

            val actual = loadSqlConfiguration(testDirectory, saveDefault = false)

            assertEquals(SqlConfiguration("jdbc:mysql://localhost:3306/db", "", ""), actual, "value: $value")
        }
    }

    @Test
    fun `url is trimmed`() {
        writeYaml("url: '  jdbc:mysql://localhost:3306/db '\nuser: 'admin'\npassword: 'secret'\n")

        val actual = loadSqlConfiguration(testDirectory, saveDefault = false)

        assertEquals("jdbc:mysql://localhost:3306/db", actual.url)
    }

    @Test
    fun `quoted values keep surrounding whitespace`() {
        writeYaml("url: 'jdbc:mysql://localhost:3306/db'\nuser: \"  admin\"\npassword: ' secret '\n")

        val actual = loadSqlConfiguration(testDirectory, saveDefault = false)

        assertEquals(SqlConfiguration("jdbc:mysql://localhost:3306/db", "  admin", " secret "), actual)
    }

    @Test
    fun `block scalar keeps its final line break unless stripped`() {
        writeYaml("url: 'jdbc:mysql://localhost:3306/db'\nuser: |-\n  admin\npassword: |\n  secret\n")

        val actual = loadSqlConfiguration(testDirectory, saveDefault = false)

        assertEquals(SqlConfiguration("jdbc:mysql://localhost:3306/db", "admin", "secret\n"), actual)
    }

    @Test
    fun `non-scalar key throws ConfigException`() {
        val dbConfig = writeYaml("url: 'jdbc:mysql://localhost:3306/db'\nuser: 'admin'\npassword: 'secret'\n? [a, b]\n: c\n")

        val exception = assertFailsWith<ConfigException> {
            loadSqlConfiguration(testDirectory, saveDefault = false)
        }
        assertEquals(dbConfig, exception.path)
    }

    @Test
    fun `list value throws ConfigException asking for a single value`() {
        val dbConfig = writeYaml("url: 'jdbc:mysql://localhost:3306/db'\nuser: 'admin'\npassword:\n  - one\n  - two\n")

        val exception = assertFailsWith<ConfigException> {
            loadSqlConfiguration(testDirectory, saveDefault = false)
        }
        assertEquals(dbConfig, exception.path)
        assertTrue(exception.message!!.contains("'password'"), "Message should name the key: ${exception.message}")
        assertTrue(exception.message!!.contains("single (quoted) value"), "Unexpected message: ${exception.message}")
    }

    @Test
    fun `mapping value throws ConfigException asking for a single value`() {
        writeYaml("url: 'jdbc:mysql://localhost:3306/db'\nuser:\n  name: admin\npassword: 'secret'\n")

        val exception = assertFailsWith<ConfigException> {
            loadSqlConfiguration(testDirectory, saveDefault = false)
        }
        assertTrue(exception.message!!.contains("'user'"), "Message should name the key: ${exception.message}")
        assertTrue(exception.message!!.contains("single (quoted) value"), "Unexpected message: ${exception.message}")
    }

    @Test
    fun `duplicate key throws ConfigException`() {
        val dbConfig = writeYaml("url: 'jdbc:mysql://localhost:3306/db'\nuser: 'admin'\npassword: 'one'\npassword: 'two'\n")

        val exception = assertFailsWith<ConfigException> {
            loadSqlConfiguration(testDirectory, saveDefault = false)
        }
        assertEquals(dbConfig, exception.path)
        assertTrue(exception.message!!.contains("Duplicate key 'password'"), "Unexpected message: ${exception.message}")
    }

    // ========================= Blank values =========================

    @Test
    fun `empty user and password are allowed`() {
        writeYaml("url: 'jdbc:sqlite:plugins/Test/data.db'\nuser: ''\npassword: ''\n")

        val actual = loadSqlConfiguration(testDirectory, saveDefault = false)

        assertEquals(SqlConfiguration("jdbc:sqlite:plugins/Test/data.db", "", ""), actual)
    }

    @Test
    fun `user and password without a value are read as empty`() {
        writeYaml("url: 'jdbc:sqlite:plugins/Test/data.db'\nuser:\npassword:\n")

        val actual = loadSqlConfiguration(testDirectory, saveDefault = false)

        assertEquals(SqlConfiguration("jdbc:sqlite:plugins/Test/data.db", "", ""), actual)
    }

    @Test
    fun `blank url throws ConfigException saying url is empty`() {
        for (url in listOf("", "''", "'   '")) {
            val dbConfig = writeYaml("url: $url\nuser: 'admin'\npassword: 'secret'\n")

            val exception = assertFailsWith<ConfigException>("url: $url") {
                loadSqlConfiguration(testDirectory, saveDefault = false)
            }
            assertEquals(dbConfig, exception.path)
            assertTrue(exception.message!!.contains("'url' is empty"), "Unexpected message: ${exception.message}")
        }
    }

    @Test
    fun `url set to YAML null throws ConfigException saying url is empty`() {
        for (url in listOf("~", "null", "Null", "NULL")) {
            val dbConfig = writeYaml("url: $url\nuser: 'admin'\npassword: 'secret'\n")

            val exception = assertFailsWith<ConfigException>("url: $url") {
                loadSqlConfiguration(testDirectory, saveDefault = false)
            }
            assertEquals(dbConfig, exception.path)
            assertTrue(exception.message!!.contains("'url' is empty"), "Unexpected message: ${exception.message}")
        }
    }

    @Test
    fun `missing url key throws ConfigException`() {
        writeYaml("user: 'admin'\npassword: 'secret'\n")

        val exception = assertFailsWith<ConfigException> {
            loadSqlConfiguration(testDirectory, saveDefault = false)
        }
        assertTrue(exception.message!!.contains("Missing required key 'url'"), "Unexpected message: ${exception.message}")
    }

    // ========================= Header comments =========================

    @Test
    fun `default configuration starts with header comments`() {
        loadSqlConfiguration(testDirectory)

        val content = Files.readString(testDirectory.resolve("database.yml"))
        assertTrue(content.startsWith(EXPECTED_HEADER), "Unexpected file content:\n$content")
    }

    @Test
    fun `migrated configuration starts with header comments`() {
        writeLegacy("url=jdbc:mysql://database.test:3306/database\nuser=testuser\npassword=testpassword\n")

        loadSqlConfiguration(testDirectory)

        val content = Files.readString(testDirectory.resolve("database.yml"))
        assertTrue(content.startsWith(EXPECTED_HEADER), "Unexpected file content:\n$content")
    }

    @Test
    fun `migrated values with special characters load back unchanged`() {
        val expected = SqlConfiguration(
            url = "jdbc:mysql://database.test:3306/database?useSSL=false&serverTimezone=UTC",
            user = "0123",
            password = "007007: #'\"\\ ü [x] {y}"
        )
        val legacyProperties = Properties().apply {
            setProperty("url", expected.url)
            setProperty("user", expected.user)
            setProperty("password", expected.password)
        }
        Files.newBufferedWriter(testDirectory.resolve("database.properties"), Charsets.UTF_8).use { writer ->
            legacyProperties.store(writer, null)
        }

        assertEquals(expected, loadSqlConfiguration(testDirectory))
        assertEquals(expected, loadSqlConfiguration(testDirectory), "Second load should read the migrated file")
    }

    @Test
    fun `migrated values with line breaks load back unchanged`() {
        val expected = SqlConfiguration(
            url = "jdbc:mysql://database.test:3306/database",
            user = "a\n\u0085b",
            password = "a\nb\r\nc\u0085d\u2028e\u2029f"
        )
        writeLegacy("url=${expected.url}\nuser=a\\n\\u0085b\npassword=a\\nb\\r\\nc\\u0085d\\u2028e\\u2029f\n")

        assertEquals(expected, loadSqlConfiguration(testDirectory))
        assertEquals(expected, loadSqlConfiguration(testDirectory), "Second load should read the migrated file")
    }

    @Test
    fun `long values are written on one line`() {
        val password = "a long password with spaces that does not fit into the eighty columns of a line"
        writeLegacy("url=jdbc:mysql://database.test:3306/database\nuser=testuser\npassword=$password\n")

        val actual = loadSqlConfiguration(testDirectory)

        val content = Files.readString(testDirectory.resolve("database.yml"))
        assertTrue(content.lines().contains("password: $password"), "Unexpected file content:\n$content")
        assertEquals(password, actual.password)
    }

    // ========================= Legacy migration =========================

    @Test
    fun `legacy file without url throws ConfigException naming the legacy file`() {
        for (content in listOf("user=testuser\npassword=testpassword\n", "url=\nuser=testuser\n")) {
            val legacyConfig = writeLegacy(content)

            val exception = assertFailsWith<ConfigException>(content) {
                loadSqlConfiguration(testDirectory)
            }
            assertEquals(legacyConfig, exception.path)
            assertTrue(exception.message!!.contains(legacyConfig.toString()), "Unexpected message: ${exception.message}")
            assertTrue(exception.message!!.contains("'url'"), "Unexpected message: ${exception.message}")
            assertTrue(Files.exists(legacyConfig), "Legacy file must be kept when it could not be migrated")
            assertFalse(Files.exists(testDirectory.resolve("database.yml")), "No database.yml should be written")
        }
    }

    @Test
    fun `legacy file without user and password migrates them as empty`() {
        val legacyConfig = writeLegacy("url=jdbc:sqlite:plugins/Test/data.db\n")

        val actual = loadSqlConfiguration(testDirectory)

        assertEquals(SqlConfiguration("jdbc:sqlite:plugins/Test/data.db", "", ""), actual)
        assertFalse(Files.exists(legacyConfig), "Legacy file should be removed after migration")
    }

    @Test
    fun `empty legacy file throws ConfigException naming the legacy file`() {
        val legacyConfig = writeLegacy("")

        val exception = assertFailsWith<ConfigException> {
            loadSqlConfiguration(testDirectory)
        }
        assertEquals(legacyConfig, exception.path)
        assertTrue(exception.message!!.contains(legacyConfig.toString()), "Unexpected message: ${exception.message}")
    }

    @Test
    fun `legacy file with malformed unicode escape throws ConfigException naming the legacy file`() {
        val legacyConfig = writeLegacy("url=jdbc:mysql://database.test:3306/database\npassword=\\u12\n")

        val exception = assertFailsWith<ConfigException> {
            loadSqlConfiguration(testDirectory)
        }
        assertEquals(legacyConfig, exception.path)
        assertTrue(exception.message!!.contains(legacyConfig.toString()), "Unexpected message: ${exception.message}")
        assertIs<IllegalArgumentException>(exception.cause)
        assertTrue(Files.exists(legacyConfig), "Legacy file must be kept when it could not be migrated")
    }

    @Test
    fun `unreadable legacy file throws ConfigException naming the legacy file`() {
        val legacyConfig = Files.createDirectory(testDirectory.resolve("database.properties"))

        val exception = assertFailsWith<ConfigException> {
            loadSqlConfiguration(testDirectory)
        }
        assertEquals(legacyConfig, exception.path)
        assertTrue(exception.message!!.contains(legacyConfig.toString()), "Unexpected message: ${exception.message}")
        assertIs<IOException>(exception.cause)
    }

    @Test
    fun `legacy file in ISO-8859-1 is migrated with its special characters`() {
        val legacyConfig = testDirectory.resolve("database.properties")
        Files.write(
            legacyConfig,
            "url=jdbc:mysql://database.test:3306/database\nuser=jürgen\npassword=pässwörd\n".toByteArray(Charsets.ISO_8859_1)
        )

        val actual = loadSqlConfiguration(testDirectory)

        assertEquals(SqlConfiguration("jdbc:mysql://database.test:3306/database", "jürgen", "pässwörd"), actual)
        assertFalse(Files.exists(legacyConfig), "Legacy file should be removed after migration")
    }

    @Test
    fun `legacy file with byte order mark is migrated`() {
        val legacyConfig = writeLegacy("\uFEFFurl=jdbc:mysql://database.test:3306/database\nuser=testuser\npassword=testpassword\n")

        val actual = loadSqlConfiguration(testDirectory)

        assertEquals(SqlConfiguration("jdbc:mysql://database.test:3306/database", "testuser", "testpassword"), actual)
        assertFalse(Files.exists(legacyConfig), "Legacy file should be removed after migration")
    }

    @Test
    fun `legacy values are trimmed when migrating`() {
        val legacyConfig = writeLegacy("url=jdbc:mysql://database.test:3306/database \nuser=testuser\\ \npassword=testpassword \n")
        val expected = SqlConfiguration("jdbc:mysql://database.test:3306/database", "testuser", "testpassword")

        assertEquals(expected, loadSqlConfiguration(testDirectory))
        assertEquals(expected, loadSqlConfiguration(testDirectory), "Second load should read the migrated file")
        assertFalse(Files.exists(legacyConfig), "Legacy file should be removed after migration")
    }

    private fun writeYaml(content: String): Path =
        Files.writeString(testDirectory.resolve("database.yml"), content)

    private fun writeLegacy(content: String): Path =
        Files.writeString(testDirectory.resolve("database.properties"), content)

    private companion object {
        const val EXPECTED_HEADER =
            "# Kraftlin database configuration (YAML).\n" +
                "# Keys: url, user, password. Example URL: jdbc:mysql://host:3306/dbname\n" +
                "# Keep credentials private; do not share this file.\n"
    }
}
