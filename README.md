# Kraftlin

[![Build Status](https://github.com/kraftlin/kraftlin/actions/workflows/build.yml/badge.svg)](https://github.com/kraftlin/kraftlin/actions)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kraftlin/kraftlin-command-core.svg?label=Maven%20Central)](https://central.sonatype.com/namespace/io.github.kraftlin)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A set of focused, type-safe Kotlin libraries for building Minecraft plugins — with first-class support for Paper and
Adventure.

Kraftlin provides small, composable modules for common plugin concerns like commands, configuration, and messages,
designed to be expressive, explicit, and easy to reason about.


## ✨ Why Kraftlin?

Minecraft plugin development is powerful — but the APIs are mostly Java-first, builder-heavy, string-based, and only
weakly typed.

Kraftlin gives you:

- Kotlin-native DSLs where structure matters
- Strong typing for arguments, configuration, and message components
- Thin wrappers over existing APIs — no hidden magic
- Modular design: use only what you need

The goal is not to replace existing systems but to make them nicer to use from Kotlin.


## 📦 Modules

Kraftlin is a collection of independent modules that can be used individually or in combination.

### Command

| Module                   | Description                                                           |
|--------------------------|-----------------------------------------------------------------------|
| `kraftlin-command-core`  | Kotlin DSL over Mojang Brigadier (no Minecraft or Paper dependencies) |
| `kraftlin-command-paper` | Paper integration: typed argument builders and context access         |

### Config (coming soon)

| Module                  | Description                                       |
|-------------------------|---------------------------------------------------|
| `kraftlin-config-core`  | Type-safe Kotlin DSL for structured configuration |
| `kraftlin-config-paper` | Paper-specific config loading helpers             |

### Message (coming soon)

| Module                   | Description                                  |
|--------------------------|----------------------------------------------|
| `kraftlin-message-core`  | Kotlin DSL for building Adventure components |
| `kraftlin-message-paper` | Paper-specific message helpers               |

Each module is published independently and can be used on its own.


## 🎯 Design goals

- Kotlin-first APIs
- Strong typing where it matters
- Explicit behavior and predictable control flow
- Minimal abstraction overhead
- Easy debugging and integration with existing tools


## ⚙ Requirements

- Java 21+
- Kotlin 2+
- Paper 1.21+ (for Paper modules)
- Adventure (for message module)


## 📥 Installation

Artifacts are published on Maven Central.

Example (Gradle Kotlin DSL):

```kotlin
dependencies {
    implementation("io.github.kraftlin:kraftlin-command-paper:${kraftlinVersion}")
}
```

Replace the artifact ID depending on the module you need.


## 🔢 Versioning & Stability

This project follows semantic versioning.

**Status:** Early-stage (0.x).  
Minor versions may contain breaking changes while the APIs are expanded and refined.
After 1.0.0, only major versions will contain breaking changes.

Kraftlin is the integration of multiple internal libraries that have been developed over years from practical needs.
It is actively used in production on our own server across multiple plugins.


## 🚀 Usage

Usage examples and guides are provided per module:

- `kraftlin-command-*` → see [kraftlin-command-paper/README.md](kraftlin-command-paper/README.md)
- `kraftlin-config-*` → (coming soon)
- `kraftlin-message` → (coming soon)

Each module has its own README with examples and API details.


## 🧠 Philosophy

Kraftlin does not replace Minecraft APIs — it wraps them.

It provides:

- Clean and readable Kotlin DSLs over existing systems
- Typed access to otherwise string-based or weakly typed data
- A small number of helpers and runtime checks where static typing is not possible

Under the hood, Kraftlin builds on standard Brigadier, Adventure, and platform APIs.  
There is no reflection, code generation, or runtime proxying — just thin wrappers and structured builders.


## 🤝 Contributing

Contributions and ideas are welcome and appreciated!
Open an issue or submit a pull request if you have feedback or suggestions.


## 📄 License

Apache-2.0
