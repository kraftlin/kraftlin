package io.github.kraftlin.config

/**
 * Access configuration for a database.
 *
 * @property url The JDBC database url
 * @property user The username used to access the database
 * @property password The database password for [user]
 */
public data class SqlConfiguration(val url: String, val user: String, val password: String) {

    override fun toString(): String {
        return "SqlConfiguration(url='$url', user='$user', password='********')"
    }
}
