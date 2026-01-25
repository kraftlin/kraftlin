# kraftlin-command-paper

Paper integration module for Kraftlin commands.

This module adds Paper-specific argument builders, context accessors, and registration helpers on top of the core
`kraftlin-command-core` module.

## Installation

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.kraftlin:kraftlin-command-paper:${kraftlinVersion}")
}
```

## Usage

```kotlin
val command = kraftlinCommand("demo") {
    requiresPermission("demo.use")
    player("target") {
        executes { sender, context ->
            val target = context.player("target")
            target.sendMessage("Hello ${target.name}")
        }
    }
}
```

## Comparison

### Kraftlin

```kotlin
val command = kraftlinCommand("demo") {
    requiresPermission("demo.use")
    player("target") {
        executes { sender, context ->
            val target = context.player("target")
            target.sendMessage("Hello ${target.name}")
        }
    }
}
```

### Brigadier + Paper

```kotlin
val command = Commands.literal("demo")
    .then(
        Commands.argument("target", ArgumentTypes.player())
            .executes { context ->
                val source = context.source
                val sender = source.sender
                val targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver::class.java)
                val target = targetResolver.resolve(source).first()
                target.sendMessage("Hello")
                sender.sendMessage("Sent hello to ${target.name}")
                Command.SINGLE_SUCCESS
            }
    )
    .build()
```

Kraftlin removes:

- manual resolver extraction (`getArgument(..., PlayerSelectorArgumentResolver::class.java)`)
- manual resolution (`resolve(source).first()`)
- leaking resolver/selector types into your command logic

### Registration

```kotlin
override fun onEnable() {
    registerKraftlinCommands {
        command(
            node = command,
            description = "Demo command",
            aliases = listOf("d", "test")
        )
    }
}
```

If you want aliases to override existing commands, including those from vanilla and other plugins, set `overrideAliases = true` when building the command.
