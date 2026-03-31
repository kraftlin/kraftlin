# Module kraftlin-config-paper

Paper-specific config loading helpers.

Provides [io.github.kraftlin.config.paper.wrapConfig] for binding to a Paper plugin's `config.yml`
and [io.github.kraftlin.config.paper.loadSqlConfiguration] for database configuration.

# Package io.github.kraftlin.config.paper

- [io.github.kraftlin.config.paper.wrapConfig]: Wraps a Paper plugin's data folder config.
- [io.github.kraftlin.config.paper.loadSqlConfiguration]: Loads database settings from `database.yml`.
- [io.github.kraftlin.config.paper.AbstractBukkitConfig]: Extends config with Paper-specific types like Material sets.
