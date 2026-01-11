# Module kraftlin-command-paper

Paper-specific Brigadier DSL extensions and argument helpers.

This module integrates the core DSL with
Paper’s [Brigadier Command API](https://docs.papermc.io/paper/dev/api/command-api/)
and [Adventure](https://docs.papermc.io/adventure/) types. It provides typed argument builders, resolvers, and accessors
for common Paper and Minecraft concepts.

## Core Types

- [io.github.kraftlin.command.paper.kraftlinCommand]: Paper-specific entry point for commands.
- [io.github.kraftlin.command.paper.PaperLiteralNode]: Type alias for [io.github.kraftlin.command.LiteralNode] using Paper's
  `CommandSourceStack`.
- [io.github.kraftlin.command.paper.PaperArgumentNode]: Type alias for [io.github.kraftlin.command.ArgumentNode] using Paper's
  `CommandSourceStack`.
- [io.github.kraftlin.command.paper.PaperContext]: Type alias for [io.github.kraftlin.command.KContext] using Paper's
  `CommandSourceStack`.

# Package io.github.kraftlin.command.paper

Paper integration layer for the core DSL.

This package provides:

- Registration helpers like [io.github.kraftlin.command.paper.registerKraftlinCommands] for Paper commands.
- Extensions for [io.github.kraftlin.command.paper.PaperLiteralNode] such as [io.github.kraftlin.command.paper.executesPlayer]
  and [io.github.kraftlin.command.paper.requiresPermission].
- Access to the underlying Paper `CommandSourceStack` via [io.github.kraftlin.command.paper.PaperSource].

# Package io.github.kraftlin.command.paper.arguments

Public DSL for defining and accessing command arguments in Paper commands.

This package contains extension functions for [io.github.kraftlin.command.paper.PaperLiteralNode] to add various argument types,
for example:

- [io.github.kraftlin.command.paper.arguments.entity] and [io.github.kraftlin.command.paper.arguments.player] for selectors.
- [io.github.kraftlin.command.paper.arguments.blockPosition] and [io.github.kraftlin.command.paper.arguments.finePosition] for coordinates.
- [io.github.kraftlin.command.paper.arguments.rotation] and [io.github.kraftlin.command.paper.arguments.angle] for orientation.
- [io.github.kraftlin.command.paper.arguments.component] and [io.github.kraftlin.command.paper.arguments.signedMessage] for Adventure chat
  types.
- [io.github.kraftlin.command.paper.arguments.choice] and [io.github.kraftlin.command.paper.arguments.enum] for custom string-based
  selections.
- [io.github.kraftlin.command.paper.arguments.playerProfiles] and [io.github.kraftlin.command.paper.arguments.uuid] for identity.

Each builder has a matching accessor on [io.github.kraftlin.command.paper.PaperContext] to retrieve the parsed values.

Refer to the [paper arguments documentation](https://docs.papermc.io/paper/dev/command-api/arguments/minecraft/) for a
detailed description of available argument types and usage examples.

# Package io.github.kraftlin.command.paper.arguments.internal

Internal implementation details for argument types and resolvers.

**Warning:** This package is **not** part of the public API and may change without notice.
