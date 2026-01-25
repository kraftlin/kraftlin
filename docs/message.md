# Message

The message module provides a Kotlin DSL for building Adventure components in a declarative, type-safe manner. The Paper integration adds extension functions for sending messages directly to players and command senders.

## Modules

- **`kraftlin-message-core`**: Platform-agnostic DSL for building Adventure components
- **`kraftlin-message-paper`**: Paper integration with CommandSender extensions

## Getting Started

Build a simple message:

```kotlin
val msg = message {
    text("Hello ")
    text("World!") {
        color(NamedTextColor.GOLD)
        bold()
    }
}
```

With Paper integration, send messages directly:

```kotlin
player.message {
    text("Click here") {
        color(NamedTextColor.GREEN)
        runCommand("/help")
        hoverMessage("Get help")
    }
}
```

## Core Concepts

### Component Building

Messages are built using nested DSL blocks:

```kotlin
message {
    text("Part 1")
    text("Part 2") {
        color(NamedTextColor.BLUE)
    }
    newLine()
    text("Part 3")
}
```

### Formatting Inheritance

Base formatting applies to all components unless overridden:

```kotlin
message {
    color(NamedTextColor.YELLOW)  // Base color
    bold()                         // Base decoration

    text("Inherits yellow + bold")
    text("Custom color") {
        color(NamedTextColor.GRAY)  // Overrides base
        // Still bold
    }
    text("Back to yellow + bold")
}
```

### Interactive Components

Add click and hover actions:

```kotlin
message {
    text("Execute command") {
        runCommand("/say hello")
        hoverMessage("Click to say hello")
    }
    text("Open link") {
        openUrl("https://example.com")
        underlined()
    }
}
```

## Text Components

### Basic Text

```kotlin
text("Simple text")

text("Formatted text") {
    color(NamedTextColor.RED)
    bold()
    italic()
}
```

### Color Shorthand

```kotlin
text("Colored", NamedTextColor.BLUE)
```

### Convenience Methods

```kotlin
message {
    text("Line 1")
    newLine()  // Add line break
    text("Line 2")
    space()    // Add space
    text("after space")
}
```

## Styling

### Colors

```kotlin
text("Text") {
    color(NamedTextColor.GOLD)
    // or
    color(TextColor.color(0xFF5733))
}
```

### Decorations

All standard text decorations are supported:

```kotlin
text("Formatted") {
    bold()
    italic()
    underlined()
    strikeThrough()
    obfuscated()
}
```

## Click Events

Only one click action per component (mutually exclusive):

```kotlin
// Execute command as the clicking player
text("Execute") { runCommand("/gamemode creative") }

// Insert command into chat bar (doesn't execute)
text("Suggest") { suggestCommand("/msg ") }

// Open URL dialog
text("Link") { openUrl("https://example.com") }

// Copy text to clipboard
text("Copy") { copyToClipboard("Copied text") }
```

## Hover Events

Only one hover event per component:

### Simple Text Hover

```kotlin
text("Hover me") {
    hoverMessage("Simple tooltip")
}
```

### Formatted Hover

```kotlin
text("Hover me") {
    hoverMessage("Tooltip text") {
        color(NamedTextColor.GOLD)
        bold()
    }
}
```

### Multi-Component Hover

```kotlin
text("Hover me") {
    hoverMessage {
        text("Line 1") { color(NamedTextColor.GOLD) }
        newLine()
        text("Line 2") { color(NamedTextColor.GRAY) }
    }
}
```

### Custom Hover Events

```kotlin
text("Item") {
    hoverEvent(itemStack.asHoverEvent())
}
```

## Insertion

Text inserted into chat on shift+click (can be combined with other actions):

```kotlin
text("Insert text") {
    insert("This text is inserted")
    runCommand("/help")  // Can combine with click action
}
```

## Legacy Support

### Converting Legacy Text

Convert `§`-style formatted text to components:

```kotlin
message {
    legacyText("§aGreen §bBlue §lbold")
}
```

### Converting to Legacy

```kotlin
val component: Component = message { /* ... */ }
val legacy: String = component.toLegacyMessage()
```

## Existing Components

Wrap existing Adventure components:

```kotlin
val existingComponent: Component = Component.text("Existing")

message {
    text(existingComponent) {
        bold()  // Add additional formatting
    }
}
```

## Paper Integration

### Sending Messages

Extension functions on `CommandSender`:

```kotlin
// Build and send complex message
player.message {
    text("Hello ") { color(NamedTextColor.GREEN) }
    text("World!")
}

// Send simple text
player.message("Simple message")

// Send colored text
player.message("Colored", NamedTextColor.BLUE)

// Send single component with formatting
player.message("Click me") {
    runCommand("/help")
    hoverMessage("Get help")
}

// Send existing component
player.message(existingComponent) {
    bold()  // Optional additional formatting
}
```

## Advanced Examples

### Command Feedback

```kotlin
player.message {
    text("Teleported to ") { color(NamedTextColor.GRAY) }
    text("${target.name}") {
        color(NamedTextColor.GOLD)
        hoverMessage {
            text("Location: ")
            text("${target.location.blockX}, ${target.location.blockY}, ${target.location.blockZ}")
        }
    }
}
```

### Interactive Menu

```kotlin
player.message {
    text("[Accept]") {
        color(NamedTextColor.GREEN)
        runCommand("/quest accept ${questId}")
        hoverMessage("Click to accept quest")
    }
    space()
    text("[Decline]") {
        color(NamedTextColor.RED)
        runCommand("/quest decline ${questId}")
        hoverMessage("Click to decline quest")
    }
}
```

### Formatted List

```kotlin
message {
    color(NamedTextColor.GRAY)
    text("Players online:")
    newLine()

    players.forEach { player ->
        text("• ${player.name}") {
            color(NamedTextColor.WHITE)
            hoverMessage {
                text("Click to teleport")
            }
            runCommand("/tp ${player.name}")
        }
        newLine()
    }
}
```

## Best Practices

1. **Use formatting inheritance** for consistent styling:
   ```kotlin
   message {
       color(NamedTextColor.GRAY)  // Set base color once
       text("Part 1")
       text("Part 2")
   }
   ```

2. **Prefer Paper extensions** for sending messages:
   ```kotlin
   player.message { /* ... */ }  // ✓ Clean and direct
   player.sendMessage(message { /* ... */ }.toChatMessage())  // ✗ Verbose
   ```

3. **Use hover messages** for additional context:
   ```kotlin
   text("Hover for details") {
       hoverMessage("Additional information here")
   }
   ```

4. **Validate actions** - The DSL will throw exceptions if you try to add multiple conflicting actions

## Why Kraftlin Messages?

Benefits over raw Adventure builders:

**Without Kraftlin:**
```kotlin
Component.text()
    .content("Click me")
    .color(NamedTextColor.GREEN)
    .clickEvent(ClickEvent.runCommand("/help"))
    .hoverEvent(HoverEvent.showText(Component.text("Help")))
    .build()
```

**With Kraftlin:**
```kotlin
message {
    text("Click me") {
        color(NamedTextColor.GREEN)
        runCommand("/help")
        hoverMessage("Help")
    }
}
```

Benefits:
- Declarative DSL matches structure of message
- Cleaner, more readable syntax
- Formatting inheritance reduces repetition
- Type-safe at compile time
- Easy composition of multi-component messages
- Validation prevents conflicting actions
