# Kraftlin

Kraftlin is a set of focused, type-safe Kotlin libraries for building Minecraft plugins — with first-class support for Paper and Adventure.

It provides small, composable modules for common plugin concerns like commands, configuration, and messages, designed to be expressive, explicit, and easy to reason about.

## Why Kraftlin?

Minecraft plugin development is powerful — but the APIs are mostly Java-first, builder-heavy, string-based, and only weakly typed.

Kraftlin gives you:

- **Kotlin-native DSLs** where structure matters
- **Strong typing** for arguments, configuration, and message components
- **Thin wrappers** over existing APIs — no hidden magic 
- **Modular design**: use only what you need

The goal is not to replace existing systems but to make them nicer to use from Kotlin.

## Installation

Artifacts are published on Maven Central:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.kraftlin:kraftlin-command-paper:${kraftlinVersion}")
    implementation("io.github.kraftlin:kraftlin-config-paper:${kraftlinVersion}")
    implementation("io.github.kraftlin:kraftlin-message-paper:${kraftlinVersion}")
}
```

## Modules

Kraftlin is a collection of independent modules that can be used individually or in combination.

### Command

Build type-safe command trees with a Kotlin DSL over Mojang Brigadier.

| Module                   | Description                                           |
|--------------------------|-------------------------------------------------------|
| `kraftlin-command-core`  | Brigadier DSL (no Minecraft or Paper dependencies)    |
| `kraftlin-command-paper` | Paper integration: typed arguments and registration   |

**Quick example:**

```kotlin
val command = kraftlinCommand("demo") {
    requiresPermission("demo.use")
    player("target") {
        executes { sender, context ->
            val target = context.player("target")
            target.sendMessage("Hello!")
        }
    }
}
```

[Learn more →](command.md)

### Config

Type-safe configuration using Kotlin property delegation.

| Module                  | Description                               |
|-------------------------|-------------------------------------------|
| `kraftlin-config-core`  | Platform-agnostic config DSL              |
| `kraftlin-config-paper` | Paper-specific YAML helpers               |

**Quick example:**

```kotlin
class Config(plugin: Plugin) : AbstractConfig(wrapConfig(plugin)) {
    val interval: Long by config("spawn_interval_ticks", 1200L)
    var message: String by config("message", "Hello!")
}

// Usage
val config = Config(this)
config.saveDefaults()
logger.info("Interval: ${config.interval}")
```

[Learn more →](config.md)

### Message

Build Adventure components using a declarative Kotlin DSL.

| Module                   | Description                              |
|--------------------------|------------------------------------------|
| `kraftlin-message-core`  | Adventure component DSL                  |
| `kraftlin-message-paper` | Paper-specific message sending helpers   |

**Quick example:**

```kotlin
player.message {
    text("Click here") {
        color(NamedTextColor.GREEN)
        runCommand("/help")
        hoverMessage("Get help")
    }
}
```

[Learn more →](message.md)

## Requirements

- Java 21+
- Kotlin 2+
- Paper 1.21+ (for Paper modules)
- Adventure (for message module)

## Design Goals

- Kotlin-first APIs
- Strong typing where it matters
- Explicit behavior and predictable control flow
- Minimal abstraction overhead
- Easy debugging and integration with existing tools

## Platform Support

Kraftlin currently supports Paper through dedicated integration modules. The core modules are platform-agnostic and can be adapted to other platforms like Velocity.

[Platform details →](platforms.md)

## Reference

- **API Documentation**: [KDoc](https://kraftlin.github.io/kraftlin/kdoc/)
- **GitHub**: [kraftlin/kraftlin](https://github.com/kraftlin/kraftlin)
- **Maven Central**: [io.github.kraftlin](https://central.sonatype.com/namespace/io.github.kraftlin)

## Philosophy

Kraftlin does not replace Minecraft APIs — it wraps them.

It provides:

- Clean and readable Kotlin DSLs over existing systems
- Typed access to otherwise string-based or weakly typed data
- A small number of helpers and runtime checks where static typing is not possible

Under the hood, Kraftlin builds on standard Brigadier, Adventure, and platform APIs. There is no reflection, code generation, or runtime proxying — just thin wrappers and structured builders.

## Status & Versioning

**Status**: Early-stage (0.x)
Minor versions may contain breaking changes while the APIs are expanded and refined. After 1.0.0, only major versions will contain breaking changes.

Kraftlin is the integration of multiple internal libraries that have been developed over years from practical needs. It is actively used in production on our own server across multiple plugins.

## License

Apache-2.0
