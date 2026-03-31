package io.github.kraftlin.command.paper

import io.github.kraftlin.command.PlatformAdapter
import org.bukkit.command.CommandSender

/**
 * Paper platform adapter for cross-platform command definitions.
 *
 * Extracts `CommandSender` from Paper's `CommandSourceStack` and delegates
 * permission checks to Bukkit's permission system.
 *
 * @see PlatformAdapter
 */
public object PaperPlatform : PlatformAdapter<PaperSource, CommandSender> {

    override fun sender(source: PaperSource): CommandSender = source.sender

    override fun hasPermission(source: PaperSource, permission: String): Boolean =
        source.sender.hasPermission(permission)
}
