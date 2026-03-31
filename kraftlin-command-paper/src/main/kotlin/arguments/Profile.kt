package io.github.kraftlin.command.paper.arguments

import com.destroystokyo.paper.profile.PlayerProfile
import io.github.kraftlin.command.argument
import io.github.kraftlin.command.paper.PaperArgumentNode
import io.github.kraftlin.command.paper.PaperContext
import io.github.kraftlin.command.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver
import java.util.*


/** Adds a player profile list argument. @see [PaperContext.playerProfiles] */
public fun PaperLiteralNode.playerProfiles(
    name: String,
    block: PaperArgumentNode<PlayerProfileListResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.playerProfiles(), block)

/** Returns the resolved collection of [PlayerProfile]s. @see [PaperLiteralNode.playerProfiles] */
public fun PaperContext.playerProfiles(name: String): Collection<PlayerProfile> =
    rawContext.getArgument(name, PlayerProfileListResolver::class.java).resolve(rawContext.source)


/** Adds a [UUID] argument. @see [PaperContext.uuid] */
public fun PaperLiteralNode.uuid(
    name: String,
    block: PaperArgumentNode<UUID>.() -> Unit,
): Unit = argument(name, ArgumentTypes.uuid(), block)

/** Returns the parsed [UUID]. @see [PaperLiteralNode.uuid] */
public fun PaperContext.uuid(name: String): UUID = rawContext.getArgument<UUID>(name, UUID::class.java)
