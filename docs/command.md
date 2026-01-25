# Command

The command module provides a Kotlin DSL over Mojang Brigadier for building type-safe command trees. The Paper integration adds typed argument builders, context accessors, and registration utilities.

## Modules

- **`kraftlin-command-core`**: Platform-agnostic Brigadier DSL (no Minecraft dependencies)
- **`kraftlin-command-paper`**: Paper integration with typed arguments and registration

## Getting Started

```kotlin
val command = kraftlinCommand("demo") {
    requiresPermission("demo.use")
    player("target") {
        executes { sender, context ->
            val target = context.player("target")
            sender.sendMessage("Sent hello to ${target.name}")
            target.sendMessage("Hello!")
        }
    }
}
```

Register in your plugin:

```kotlin
override fun onEnable() {
    registerKraftlinCommands(command)
}
```

## Core Concepts

### Command Structure

Commands are built using nested DSL blocks:

```kotlin
kraftlinCommand("mycommand") {
    // Literal sub-commands
    literal("info") {
        executes { sender, ctx -> /* ... */ }
    }

    // Typed arguments
    integer("amount") {
        executes { sender, ctx ->
            val amount = ctx.integer("amount")
            sender.sendMessage("Amount: $amount")
        }
    }
}
```

### Dual-Function Pattern

Every argument type has two functions:
1. **Builder function**: Adds the argument to the command tree
2. **Accessor function**: Retrieves the typed value from context

```kotlin
// Builder - adds argument node
player("target") {
    executes { sender, ctx ->
        // Accessor - retrieves typed Player
        val target = ctx.player("target")
    }
}
```

### Execution

```kotlin
// Standard execution (automatic SINGLE_SUCCESS)
executes { sender, context -> /* ... */ }

// Custom result code
executesResult { sender, context ->
    if (condition) Command.SINGLE_SUCCESS else 0
}

// Player-only execution
executesPlayer { player, context -> /* ... */ }
```

### Requirements

Requirements can be chained and are AND-ed together:

```kotlin
kraftlinCommand("admin") {
    requiresPermission("admin.use")
    requiresPlayer()
    requires { /* custom check */ }
    // All must pass
}
```

## Argument Types

### Core Types

Available in `kraftlin-command-core`:

- `string(name)`, `word(name)`, `greedyString(name)` - String arguments
- `integer(name, min?, max?)`, `long(name, min?, max?)` - Integers with optional bounds
- `float(name, min?, max?)`, `double(name, min?, max?)` - Floats with optional bounds
- `boolean(name)` - Boolean values

### Paper Types

Available in `kraftlin-command-paper`:

**Players & Entities:**
- `player(name)` → `Player` - Single player selector
- `players(name)` → `List<Player>` - Multiple players
- `entity(name)` → `Entity` - Single entity
- `entities(name)` → `List<Entity>` - Multiple entities

**Positions:**
- `blockPosition(name)` → `BlockPosition`
- `finePosition(name, centerIntegers?)` → `FinePosition`
- `columnBlockPosition(name)`, `columnFinePosition(name)` - 2D positions

**Gameplay:**
- `world(name)` → `World`
- `blockState(name)` → `BlockState`
- `itemStack(name)` → `ItemStack`
- `gameMode(name)` → `GameMode`
- `time(name, minTime?)` → `Int` - Game time

**Messages & Components:**
- `component(name)` → `Component` - Adventure component
- `signedMessage(name)` → `CompletableFuture<SignedMessage>` - Preserves signatures
- `style(name)` → `Style`
- `namedColor(name)` → `NamedTextColor`
- `hexColor(name)` → `TextColor`

**Resources & Keys:**
- `key(name)` → `Key`
- `namespacedKey(name)` → `NamespacedKey`
- `resource(name, registryKey)` → `T`
- `resourceKey(name, registryKey)` → `TypedKey<T>`

**Other:**
- `choice(name, values)` → `String` - Custom string choice with validation
- `enum<E>(name, values?)` → `E` - Enum-backed choice
- `integerRange(name)`, `doubleRange(name)` - Numeric ranges
- `rotation(name)`, `angle(name)` - Rotation/angles
- `playerProfiles(name)`, `uuid(name)` - Player profiles and UUIDs

See KDoc for the complete list.

## Advanced Features

### Suggestions

```kotlin
word("option") {
    suggestsStatic("foo", "bar", "baz")
    executes { sender, ctx -> /* ... */ }
}

// Custom suggestions
word("dynamic") {
    suggests { context, builder ->
        listOf("option1", "option2").forEach { builder.suggest(it) }
        builder.buildFuture()
    }
}
```

### Aliases

```kotlin
literal("info", "i", "information") {
    executes { sender, ctx -> /* ... */ }
}

// Override existing commands (use carefully)
kraftlinCommand("help", overrideAliases = true) {
    // ...
}
```

### Custom Argument Types

Use `choice()` for custom validated strings:

```kotlin
choice("difficulty", listOf("easy", "normal", "hard")) {
    executes { sender, ctx ->
        val difficulty = ctx.choice("difficulty")
    }
}
```

Use `enum()` for enum-backed arguments:

```kotlin
enum<GameMode>("mode") {
    executes { sender, ctx ->
        val mode = ctx.enum<GameMode>("mode")
    }
}
```

## Why Kraftlin Commands?

Kraftlin removes boilerplate and type-unsafe patterns from Brigadier:

**Without Kraftlin:**
```kotlin
Commands.argument("target", ArgumentTypes.player())
    .executes { context ->
        val source = context.source
        val resolver = context.getArgument("target", PlayerSelectorArgumentResolver::class.java)
        val target = resolver.resolve(source).first()
        // ...
    }
```

**With Kraftlin:**
```kotlin
player("target") {
    executes { sender, context ->
        val target = context.player("target")
        // ...
    }
}
```

Benefits:
- No manual resolver extraction or resolution
- Typed context accessors instead of raw `getArgument()`
- Cleaner syntax with Kotlin DSL
- Type safety at compile time
- No resolver types leaking into command logic
