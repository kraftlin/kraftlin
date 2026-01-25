# Config

The config module provides a type-safe Kotlin DSL for structured configuration using property delegation. The Paper integration adds helpers for working with YAML files and Bukkit-specific types.

## Modules

- **`kraftlin-config-core`**: Platform-agnostic config DSL with type-safe property delegation
- **`kraftlin-config-paper`**: Paper integration with YAML wrappers and Bukkit type support

## Getting Started

Create a config class extending `AbstractConfig`:

```kotlin
class Config(plugin: Plugin) : AbstractConfig(wrapConfig(plugin)) {
    val interval: Long by config("spawn_interval_ticks", 1200L)
    val chance: Double by config("spawn_chance", 0.25)
    var message: String by config("message", "Hello!")
}
```

Use in your plugin:

```kotlin
override fun onEnable() {
    val config = Config(this)
    config.saveDefaults()  // Write defaults if not present

    logger.info("Interval: ${config.interval}")
    config.message = "Updated!"
    config.save()  // Persist changes
}
```

## Core Concepts

### Property Delegation

Configuration values are bound to Kotlin properties using delegation:

```kotlin
val readOnly: String by config("path.to.value", "default")
var mutable: Int by config("path", 42)  // Can be changed and saved
```

### Type Safety

The module provides compile-time type checking for all config values:

```kotlin
val count: Int by config("count", 10)          // ✓ Int
val items: List<String> by config("items", listOf("a", "b"))  // ✓ List<String>
// val wrong: Int by config("count", "text")   // ✗ Compile error
```

### Lifecycle

```kotlin
val config = Config(plugin)

// First time setup
config.saveDefaults()  // Write defaults to file if not present

// After user edits file
config.reloadConfig()  // Reload from disk, invalidate cache

// After programmatic changes
config.message = "New value"
config.save()  // Write back to disk
```

## Supported Types

### Primitives
- `Boolean`, `Int`, `Long`, `Double`, `String`

### Built-in Complex Types
- `UUID` - Stored as string
- `Enum<T>` - Stored as lowercase name (supports both `UPPER_CASE` and `kebab-case`)
- `List<T>` - For primitives, enums, UUIDs, and custom types
- `Map<String, T>` - String-keyed maps with typed values

### Custom Types

Use `serialize` and `deserialize` functions for custom types:

```kotlin
val customValue: LocalDateTime by config(
    path = "date",
    default = LocalDateTime.now(),
    serialize = { it.toString() },
    deserialize = { LocalDateTime.parse(it) }
)
```

Works with lists too:

```kotlin
val dates: List<LocalDateTime> by config(
    path = "dates",
    default = listOf(LocalDateTime.now()),
    serialize = { it.toString() },
    deserialize = { LocalDateTime.parse(it) }
)
```

### Paper-Specific Types

Available in `kraftlin-config-paper`:

```kotlin
class BukkitConfig(plugin: Plugin) : AbstractBukkitConfig(wrapConfig(plugin)) {
    // Materials and tags
    val materials: Set<Material> by config(
        "blocks",
        listOf(Material.STONE, Tag.SHULKER_BOXES)
    )
}
```

Material sets support:
- Individual materials: `STONE`, `DIAMOND_ORE`
- Material tags: `#shulker_boxes`, `#wool`
- Both with or without `minecraft:` prefix
- Returns `EnumSet<Material>` for performance

## Advanced Features

### Nested Configuration

Use inner classes for structured configuration:

```kotlin
class Config(plugin: Plugin) : AbstractConfig(wrapConfig(plugin)) {
    val spring = EventConfig("spring")
    val winter = EventConfig("winter")

    inner class EventConfig(id: String) {
        val enabled: Boolean by config("$id.enabled", true)
        val startDate: String by config("$id.start_date", "2024-01-01")
        val endDate: String by config("$id.end_date", "2024-12-31")
    }
}
```

Results in YAML:

```yaml
spring:
  enabled: true
  start_date: "2024-01-01"
  end_date: "2024-12-31"
winter:
  enabled: true
  start_date: "2024-01-01"
  end_date: "2024-12-31"
```

### Comments

Add comments to config values:

```kotlin
val interval: Long by config(
    "spawn_interval_ticks",
    1200L,
    "Time in ticks between spawns",
    "20 ticks = 1 second"
)
```

Comments are written during `saveDefaults()` if the path doesn't have existing comments.

### Multiple Files

Bind to arbitrary YAML files instead of the default `config.yml`:

```kotlin
val customConfig = object : AbstractConfig(wrapConfig(dataFolder.resolve("custom.yml"))) {
    val value: Int by config("value", 5)
}
customConfig.saveDefaults()
```

### Redundant Key Removal

Clean up old/unused config keys:

```kotlin
config.removeRedundantKeys()  // Removes keys not defined in code
```

## Database Configuration

Load database settings from a separate `database.yml`:

```kotlin
val database = loadSqlConfiguration(plugin)
// Returns SqlConfiguration(url, user, password)
```

We highly encourage this separation:
1. Reduces risk of accidentally committing sensitive data
2. Allows easy switching between database backends
3. Simplifies DevOps automation

Features:
- Auto-migrates legacy `database.properties` files
- Creates example config if none exists
- Includes helpful header comments

Example `database.yml`:

```yaml
url: jdbc:postgresql://localhost:5432/mydb
user: myuser
password: mypassword
```

## Why Kraftlin Config?

Benefits over manual YAML access:

**Without Kraftlin:**
```kotlin
val interval = config.getLong("spawn_interval_ticks", 1200L)
val items = config.getStringList("items")
// Manual type checking, scattered get/set calls
```

**With Kraftlin:**
```kotlin
class Config(plugin: Plugin) : AbstractConfig(wrapConfig(plugin)) {
    val interval: Long by config("spawn_interval_ticks", 1200L)
    val items: List<String> by config("items", listOf())
}
// Type-safe, organized, automatic defaults
```

Benefits:
- Compile-time type safety
- Centralized configuration definition
- Automatic default value management
- Clean property access throughout code
- Lazy loading with caching
- Support for custom types via serialization
