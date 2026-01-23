# Config

The config module provides a type-safe Kotlin DSL for structured configuration,
plus Paper helpers for working with `config.yml` and additional YAML files.

## Paper helpers

Use the Paper integration to bind Kotlin properties to YAML values:

```kotlin
class Config(plugin: Plugin) : AbstractConfig(wrapConfig(plugin)) {
    val interval: Long by config("spawn_interval_ticks", 1200L)
}
```

## Database configuration

Load database settings from a separate `database.yml` in the plugin data folder:

```kotlin
val database = loadSqlConfiguration(plugin)
```

We highly encourage this separation. It serves several purposes:
1. It reduces the risk of accidentally sharing sensitive data or committing it to a repository.
2. It allows you to easily switch between different database backends without changing the primary config.
3. DevOps can easily automate database configurations.

Legacy `database.properties` files are migrated automatically on first load.
