package io.github.kraftlin.command.velocity

import com.velocitypowered.api.command.CommandSource
import io.github.kraftlin.command.PlatformAdapter

/**
 * Velocity platform adapter for cross-platform command definitions.
 *
 * Velocity's `CommandSource` is used directly as the sender (no unwrapping needed)
 * and implements `PermissionSubject` for permission checks.
 *
 * @see PlatformAdapter
 */
public object VelocityPlatform : PlatformAdapter<VelocitySource, CommandSource> {

    override fun sender(source: VelocitySource): CommandSource = source

    override fun hasPermission(source: VelocitySource, permission: String): Boolean =
        source.hasPermission(permission)
}
