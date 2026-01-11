package io.github.kbrigx.paper.arguments

import com.destroystokyo.paper.profile.PlayerProfile
import io.github.kbrigx.core.argument
import io.github.kbrigx.paper.PaperArgumentNode
import io.github.kbrigx.paper.PaperContext
import io.github.kbrigx.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver
import java.util.*


public fun PaperLiteralNode.playerProfiles(
    name: String,
    block: PaperArgumentNode<PlayerProfileListResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.playerProfiles(), block)

public fun PaperContext.playerProfiles(name: String): Collection<PlayerProfile> =
    rawContext.getArgument(name, PlayerProfileListResolver::class.java).resolve(rawContext.source)


public fun PaperLiteralNode.uuid(
    name: String,
    block: PaperArgumentNode<UUID>.() -> Unit,
): Unit = argument(name, ArgumentTypes.uuid(), block)

public fun PaperContext.uuid(name: String): UUID = rawContext.getArgument<UUID>(name, UUID::class.java)
