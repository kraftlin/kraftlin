# Module kraftlin-command-paper

Paper-specific Brigadier DSL extensions and argument helpers.

This module integrates the core DSL with
Paper’s [Brigadier Command API](https://docs.papermc.io/paper/dev/api/command-api/)
and [Adventure](https://docs.papermc.io/adventure/) types. It provides typed argument builders, resolvers, and accessors
for common Paper and Minecraft concepts.

## Core Types

- [io.github.kbrigx.paper.PaperKBrigx]: Paper-specific entry point for commands.
- [io.github.kbrigx.paper.PaperLiteralNode]: Type alias for [io.github.kbrigx.core.LiteralNode] using Paper's
  `CommandSourceStack`.
- [io.github.kbrigx.paper.PaperArgumentNode]: Type alias for [io.github.kbrigx.core.ArgumentNode] using Paper's
  `CommandSourceStack`.
- [io.github.kbrigx.paper.PaperContext]: Type alias for [io.github.kbrigx.core.KContext] using Paper's
  `CommandSourceStack`.

# Package io.github.kbrigx.paper

Paper integration layer for the core DSL.

This package provides:

- Registration helpers like [io.github.kbrigx.paper.registerBrigxCommands] for Paper commands.
- Extensions for [io.github.kbrigx.paper.PaperLiteralNode] such as [io.github.kbrigx.paper.executesPlayer]
  and [io.github.kbrigx.paper.requiresPermission].
- Access to the underlying Paper `CommandSourceStack` via [io.github.kbrigx.paper.PaperSource].

# Package io.github.kbrigx.paper.arguments

Public DSL for defining and accessing command arguments in Paper commands.

This package contains extension functions for [io.github.kbrigx.paper.PaperLiteralNode] to add various argument types,
for example:

- [io.github.kbrigx.paper.arguments.entity] and [io.github.kbrigx.paper.arguments.player] for selectors.
- [io.github.kbrigx.paper.arguments.blockPosition] and [io.github.kbrigx.paper.arguments.finePosition] for coordinates.
- [io.github.kbrigx.paper.arguments.component] and [io.github.kbrigx.paper.arguments.signedMessage] for Adventure chat
  types.
- [io.github.kbrigx.paper.arguments.choice] and [io.github.kbrigx.paper.arguments.enum] for custom string-based
  selections.

Each builder has a matching accessor on [io.github.kbrigx.paper.PaperContext] to retrieve the parsed values.

Refer to the [paper arguments documentation](https://docs.papermc.io/paper/dev/command-api/arguments/minecraft/) for a
detailed description of available argument types and usage examples.

# Package io.github.kbrigx.paper.arguments.internal

Internal implementation details for argument types and resolvers.

**Warning:** This package is **not** part of the public API and may change without notice.
