# Module kraftlin-command-core

Core Kotlin DSL for building Brigadier command trees.

This module provides a thin, idiomatic Kotlin wrapper over Mojang Brigadier.
It contains no Minecraft or Paper dependencies and can be used in any Brigadier-based environment.

## Key Primitives

- [io.github.kraftlin.command.brigadierCommand]: Entry point for creating commands.
- [io.github.kraftlin.command.LiteralNode]: Base for literal command nodes.
- [io.github.kraftlin.command.ArgumentNode]: Base for required argument nodes.
- [io.github.kraftlin.command.KContext]: Wrapper around Brigadier's `CommandContext`.

# Package io.github.kraftlin.command

Core DSL primitives and node builders used to construct command trees.

This package contains:

- Entry points like [io.github.kraftlin.command.brigadierCommand] for creating command trees.
- Node builders like [io.github.kraftlin.command.literal] and [io.github.kraftlin.command.argument].
- [io.github.kraftlin.command.KContext] for idiomatic argument retrieval.
- Execution builders like [io.github.kraftlin.command.executes] and [io.github.kraftlin.command.executesResult].
- Requirement builders like [io.github.kraftlin.command.requires].

All functionality here is platform-agnostic and does not depend on Paper or Minecraft classes.
