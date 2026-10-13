# Config

The config module provides a type-safe Kotlin DSL for structured configuration using property delegation. The Paper integration adds helpers for working with YAML files and Bukkit-specific types.

## Modules

| Module                    | Description                                        |
|---------------------------|----------------------------------------------------|
| `kraftlin-config-core`    | Platform-agnostic config DSL with property delegation |
| `kraftlin-config-paper`   | Paper integration with Bukkit type support         |
| `kraftlin-config-bungee`  | BungeeCord integration                             |
| `kraftlin-config-velocity`| Velocity integration                               |

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
- `UUID` - Stored as string. Anything `UUID.fromString` accepts is read, including abbreviated forms such as `0-0-0-0-1`
- `Enum<T>` - Stored as lowercase name; read case-insensitively, ignoring surrounding whitespace and treating `-` as `_`.
  Works for enum entries declared in `UPPER_CASE`, `PascalCase` or `kebab-case`; in the file `DARK_OAK`, `dark_oak` and
  `dark-oak` all match `DARK_OAK` (`DarkOak` does not)
- `List<T>` - For primitives, enums, UUIDs, and custom types
- `Map<String, String>`, or `Map<String, T>` with `serialize`/`deserialize`

### Invalid Values

Reading a value never throws because of what a server admin wrote into the file. A value that cannot be read logs a
warning and falls back to the default; in lists the element is skipped, and in maps the entry is skipped, while the rest
is kept. A key that is present without a value (`items:`) reads as an empty list or map; scalars fall back to their
default. Warnings go to the logger passed to `wrapConfig`/`wrapPluginConfig` (default `io.github.kraftlin.config`; pass
`logger` to use your plugin's).

Loading is different: creating the config, `reloadConfig()`, `saveDefaults()` and `save()` throw `ConfigException` if
the file is not valid YAML or UTF-8, contains an alias that includes itself, or cannot be read or written. After a
failed reload the previous values stay active.

Values read as strings are returned exactly as typed: `version: 1.10` reads as `"1.10"` and `code: 007` as `"007"`.

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

Works with lists too. Give the lambda parameters explicit types there, so the list overload can be chosen:

```kotlin
val dates: List<LocalDateTime> by config(
    path = "dates",
    default = listOf(LocalDateTime.now()),
    serialize = { date: LocalDateTime -> date.toString() },
    deserialize = { text: String -> LocalDateTime.parse(text) }
)
```

If `deserialize` throws, a warning is logged and the default is used (list elements and map entries are skipped).

### Paper-Specific Types

Available in `kraftlin-config-paper`: material lists, read as a `MaterialSet`.

```kotlin
class BukkitConfig(plugin: Plugin) : AbstractBukkitConfig(wrapConfig(plugin)) {
    // Materials
    val breakable: MaterialSet by config(
        "breakable",
        listOf(Material.STONE, Material.DIRT),
        "Blocks that may be broken"
    )

    // Materials and tags
    var diggable: MaterialSet by config(
        "diggable",
        MaterialSet(listOf(Material.GRAVEL), listOf(Tag.SAND)),
        "Blocks the pet may dig"
    )
}
```

Any collection of materials works as default, `listOf(...)` being the common case. Use `MaterialSet(materials, tags)`
for a default with tags, and `materialSetOf(Material.STONE, Material.DIRT)` where a `MaterialSet` of materials is
needed.

The file lists:
- Individual materials: `stone`, `diamond_ore`
- Block and item tags: `'#shulker_boxes'`, `'#wool'`, `'#mineable/pickaxe'`. Quote them in YAML, because an unquoted
  `#` starts a comment
- Both with or without `minecraft:` prefix

```yaml
diggable:
- '#minecraft:sand'
- minecraft:gravel
```

A `MaterialSet` is an immutable `Set<Material>` that contains the listed materials and all values of the listed tags,
and also remembers what was listed: `diggable.materials` and `diggable.tags`. Tags stay tags: saving writes them back
as `'#...'` entries, not as the materials they contain. They are resolved each time the config is read (on startup and
after `reloadConfig()`), so the set picks up blocks that new Minecraft versions or datapacks add to a tag.

Like every other delegate, material lists take comments. Empty lists (`blocks: []`) read as an empty set. Unknown or
legacy materials and unknown tags are skipped with a warning.

`+` and `-` return a new `MaterialSet` and keep its tags. Adding or removing another `MaterialSet` adds or removes its
tags as well.

```kotlin
config.diggable += Material.CLAY       // add a material
config.diggable += Tag.DIRT            // add a tag
config.diggable -= Material.GRAVEL     // remove a material
config.diggable -= Tag.SAND            // remove a tag
config.save()
```

Removing a material that is not part of the set changes nothing. Removing a material that one of the set's tags
includes throws an `IllegalArgumentException` because the result could not keep the tag, e.g.
`RED_SAND is included by tag #minecraft:sand; remove the tag instead`. `diggable.tagsContaining(material)` returns the
tags of the set that include a material.

A `MaterialSet` equals every `Set<Material>` with the same materials; tags are not part of equality. Creating a
`MaterialSet` with legacy materials throws an `IllegalArgumentException`.

For a plain `List<Material>` without tags, give the type argument. It is stored like other enum lists (`stone`):

```kotlin
val order: List<Material> by config<Material>("order", listOf(Material.STONE, Material.DIRT))
```

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
  start_date: 2024-01-01
  end_date: 2024-12-31
winter:
  enabled: true
  start_date: 2024-01-01
  end_date: 2024-12-31
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

Comments are written during `saveDefaults()` if the path doesn't have existing comments. A comment containing line
breaks is split into several lines.

Comments written by server admins are kept when the file is saved: comments above keys, comments behind values,
comments on list items, blank lines and comments at the end of the file. Comment lines at the very top of the file,
separated from the first key by a blank line, are treated as the file header and stay at the top. Values that were not
changed keep their quoting. Indentation is normalized: nested keys are indented by two spaces, and list items (`- `)
start at the indentation of their key. Aliases are written as copies.

### Multiple Files

Bind to arbitrary YAML files instead of the default `config.yml`. For a path, import the core
`io.github.kraftlin.config.wrapConfig`:

```kotlin
import io.github.kraftlin.config.wrapConfig

val customConfig = object : AbstractConfig(wrapConfig(dataPath.resolve("custom.yml"))) {
    val value: Int by config("value", 5)
}
customConfig.saveDefaults()
```

`dataPath` is the plugin's data folder as a `Path` (`dataFolder` is a `File`). In a file that also imports the Paper
`wrapConfig(plugin)`, the call with a path is ambiguous; import the core function under another name there, e.g.
`import io.github.kraftlin.config.wrapConfig as wrapConfigFile`.

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
- Auto-migrates legacy `database.properties` files (the old file is deleted afterwards)
- Creates example config if none exists
- Includes helpful header comments
- All three keys `url`, `user` and `password` must be present. `user` and `password` may be empty, `~` or `null` (read
  as empty, e.g. for SQLite); `url` must not be empty
- Values are read as typed, so a password like `007007` stays `007007`; only `url` is trimmed

Example `database.yml`:

```yaml
url: jdbc:postgresql://localhost:5432/mydb
user: myuser
password: mypassword
```

## Platform Setup

Each platform provides a function to create the config wrapper (`wrapConfig` on Paper and BungeeCord,
`wrapPluginConfig` on Velocity):

=== "Paper"

    ```kotlin
    class Config(plugin: Plugin) : AbstractConfig(wrapConfig(plugin)) {
        val value: String by config("key", "default")
    }
    ```

=== "BungeeCord"

    ```kotlin
    class Config(plugin: Plugin) : AbstractConfig(wrapConfig(plugin)) {
        val value: String by config("key", "default")
    }
    ```

=== "Velocity"

    ```kotlin
    class Config(dataDirectory: Path) : AbstractConfig(wrapPluginConfig(dataDirectory)) {
        val value: String by config("key", "default")
    }
    ```

    Pass the plugin's injected `@DataDirectory` path.

The wrapper functions accept platform-specific types but all return the same `ConfigWrapper`, so
everything else (property delegation, lifecycle, types) works identically across platforms.

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
    val items: List<String> by config("items", listOf<String>())
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
