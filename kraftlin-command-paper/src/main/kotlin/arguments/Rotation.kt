@file:Suppress("UnstableApiUsage")

package io.github.kbrigx.paper.arguments

import io.github.kbrigx.core.argument
import io.github.kbrigx.paper.PaperArgumentNode
import io.github.kbrigx.paper.PaperContext
import io.github.kbrigx.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.AxisSet
import io.papermc.paper.command.brigadier.argument.resolvers.AngleResolver
import io.papermc.paper.command.brigadier.argument.resolvers.RotationResolver
import io.papermc.paper.entity.LookAnchor
import io.papermc.paper.math.Rotation
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation


public fun PaperLiteralNode.rotation(
    name: String,
    block: PaperArgumentNode<RotationResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.rotation(), block)

public fun PaperContext.rotation(name: String): Rotation =
    rawContext.getArgument(name, RotationResolver::class.java).resolve(rawContext.source)


public fun PaperLiteralNode.angle(
    name: String,
    block: PaperArgumentNode<AngleResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.angle(), block)

public fun PaperContext.angle(name: String): Float =
    rawContext.getArgument(name, AngleResolver::class.java).resolve(rawContext.source)


public fun PaperLiteralNode.axes(
    name: String,
    block: PaperArgumentNode<AxisSet>.() -> Unit,
): Unit = argument(name, ArgumentTypes.axes(), block)

public fun PaperContext.axes(name: String): AxisSet = rawContext.getArgument<AxisSet>(name, AxisSet::class.java)


public fun PaperLiteralNode.templateMirror(
    name: String,
    block: PaperArgumentNode<Mirror>.() -> Unit,
): Unit = argument(name, ArgumentTypes.templateMirror(), block)

public fun PaperContext.templateMirror(name: String): Mirror = rawContext.getArgument<Mirror>(name, Mirror::class.java)

public fun PaperLiteralNode.templateRotation(
    name: String,
    block: PaperArgumentNode<StructureRotation>.() -> Unit,
): Unit = argument(name, ArgumentTypes.templateRotation(), block)

public fun PaperContext.templateRotation(name: String): StructureRotation =
    rawContext.getArgument<StructureRotation>(name, StructureRotation::class.java)


public fun PaperLiteralNode.entityAnchor(
    name: String,
    block: PaperArgumentNode<LookAnchor>.() -> Unit,
): Unit = argument(name, ArgumentTypes.entityAnchor(), block)

public fun PaperContext.entityAnchor(name: String): LookAnchor =
    rawContext.getArgument<LookAnchor>(name, LookAnchor::class.java)
