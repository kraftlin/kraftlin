# Kraftlin Config

This module provides Paper-specific integration for `kraftlin-config-core`.
It lets you bind Kotlin properties directly to values in a Paper `config.yml` or arbitrary YAML files.

## Basic usage

```kotlin
class Config(plugin: Plugin) : AbstractConfig(wrapConfig(plugin)) {

    val interval: Long by config("spawn_interval_ticks", 1200L)
    val chance: Double by config("spawn_chance", 0.25)

    val easter = EventConfig("spring")
    val winter = EventConfig("winter")

    inner class EventConfig(id: String) {
        var enabledFrom: LocalDateTime by config("$id.start_date", LocalDateTime.now(),
            serialize = { it.toString() }, deserialize = { LocalDateTime.parse(it) })

        var enabledUntil: LocalDateTime by config("$id.end_date", LocalDateTime.now(),
            serialize = { it.toString() }, deserialize = { LocalDateTime.parse(it) })
    }
}
```

Store default values if they don't exist:

```kotlin
val config = Config(this)
config.saveDefaults()
```

Reload the config from the YAML file:

```kotlin
config.reloadConfig()
```

## Multiple files

You can bind a config to an arbitrary YAML file instead of the default `config.yml`:

```kotlin
val custom = object : AbstractConfig(wrapConfig(dataFolder.resolve("custom.yml"))) {
    val value: Int by config("value", 5)
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

## Features

- Type-safe property delegates
- Automatic default writing
- Reload and save support
- Custom serialization for arbitrary types

See `kraftlin-config-core` for the full set of supported types and delegates.
