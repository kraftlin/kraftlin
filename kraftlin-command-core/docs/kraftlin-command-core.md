# Module kraftlin-command-core

Core Kotlin DSL for building Brigadier command trees.

This module provides a thin, idiomatic Kotlin wrapper over Mojang Brigadier.
It contains no Minecraft or Paper dependencies and can be used in any Brigadier-based environment.

## Key Primitives

- [io.github.kbrigx.core.KBrigx]: Entry point for creating commands.
- [io.github.kbrigx.core.LiteralNode]: Base for literal command nodes.
- [io.github.kbrigx.core.ArgumentNode]: Base for required argument nodes.
- [io.github.kbrigx.core.KContext]: Wrapper around Brigadier's `CommandContext`.

# Package io.github.kbrigx.core

Core DSL primitives and node builders used to construct command trees.

This package contains:

- Entry points like [io.github.kbrigx.core.KBrigx.command] for creating command trees.
- Node builders like [io.github.kbrigx.core.literal] and [io.github.kbrigx.core.argument].
- [io.github.kbrigx.core.KContext] for idiomatic argument retrieval.
- Execution builders like [io.github.kbrigx.core.executes] and [io.github.kbrigx.core.executesResult].
- Requirement builders like [io.github.kbrigx.core.requires].

All functionality here is platform-agnostic and does not depend on Paper or Minecraft classes.
