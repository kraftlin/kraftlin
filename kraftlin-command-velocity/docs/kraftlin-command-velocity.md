# Module kraftlin-command-velocity

Velocity-specific Brigadier DSL extensions.

This module integrates the core DSL with Velocity's `BrigadierCommand` API, providing
typed command building and registration for Velocity proxies.

# Package io.github.kraftlin.command.velocity

Velocity integration layer for the core DSL.

- [io.github.kraftlin.command.velocity.kraftlinCommand]: Entry point for defining Velocity commands.
- [io.github.kraftlin.command.velocity.registerKraftlinCommands]: Registration helper.

# Package io.github.kraftlin.command.velocity.arguments

Velocity-specific argument helpers such as [io.github.kraftlin.command.velocity.arguments.choice]
and [io.github.kraftlin.command.velocity.arguments.enum].
