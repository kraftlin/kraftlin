@file:Suppress("UnstableApiUsage")

package io.github.kraftlin.command.paper.arguments

import io.github.kraftlin.command.argument
import io.github.kraftlin.command.paper.PaperArgumentNode
import io.github.kraftlin.command.paper.PaperContext
import io.github.kraftlin.command.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.AxisSet
import io.papermc.paper.command.brigadier.argument.resolvers.AngleResolver
import io.papermc.paper.command.brigadier.argument.resolvers.RotationResolver
import io.papermc.paper.entity.LookAnchor
import io.papermc.paper.math.Rotation
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation


/** Adds a [Rotation] (yaw, pitch) argument. @see [PaperContext.rotation] */
public fun PaperLiteralNode.rotation(
    name: String,
    block: PaperArgumentNode<RotationResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.rotation(), block)

/** Returns the resolved [Rotation]. @see [PaperLiteralNode.rotation] */
public fun PaperContext.rotation(name: String): Rotation =
    rawContext.getArgument(name, RotationResolver::class.java).resolve(rawContext.source)


/** Adds an angle argument (single float rotation). @see [PaperContext.angle] */
public fun PaperLiteralNode.angle(
    name: String,
    block: PaperArgumentNode<AngleResolver>.() -> Unit,
): Unit = argument(name, ArgumentTypes.angle(), block)

/** Returns the resolved angle in degrees. @see [PaperLiteralNode.angle] */
public fun PaperContext.angle(name: String): Float =
    rawContext.getArgument(name, AngleResolver::class.java).resolve(rawContext.source)


/** Adds an [AxisSet] argument (combination of x, y, z axes). @see [PaperContext.axes] */
public fun PaperLiteralNode.axes(
    name: String,
    block: PaperArgumentNode<AxisSet>.() -> Unit,
): Unit = argument(name, ArgumentTypes.axes(), block)

/** Returns the parsed [AxisSet]. @see [PaperLiteralNode.axes] */
public fun PaperContext.axes(name: String): AxisSet = rawContext.getArgument<AxisSet>(name, AxisSet::class.java)


/** Adds a structure [Mirror] argument. @see [PaperContext.templateMirror] */
public fun PaperLiteralNode.templateMirror(
    name: String,
    block: PaperArgumentNode<Mirror>.() -> Unit,
): Unit = argument(name, ArgumentTypes.templateMirror(), block)

/** Returns the parsed [Mirror]. @see [PaperLiteralNode.templateMirror] */
public fun PaperContext.templateMirror(name: String): Mirror = rawContext.getArgument<Mirror>(name, Mirror::class.java)

/** Adds a [StructureRotation] argument. @see [PaperContext.templateRotation] */
public fun PaperLiteralNode.templateRotation(
    name: String,
    block: PaperArgumentNode<StructureRotation>.() -> Unit,
): Unit = argument(name, ArgumentTypes.templateRotation(), block)

/** Returns the parsed [StructureRotation]. @see [PaperLiteralNode.templateRotation] */
public fun PaperContext.templateRotation(name: String): StructureRotation =
    rawContext.getArgument<StructureRotation>(name, StructureRotation::class.java)


/** Adds a [LookAnchor] (eyes or feet) argument. @see [PaperContext.entityAnchor] */
public fun PaperLiteralNode.entityAnchor(
    name: String,
    block: PaperArgumentNode<LookAnchor>.() -> Unit,
): Unit = argument(name, ArgumentTypes.entityAnchor(), block)

/** Returns the parsed [LookAnchor]. @see [PaperLiteralNode.entityAnchor] */
public fun PaperContext.entityAnchor(name: String): LookAnchor =
    rawContext.getArgument<LookAnchor>(name, LookAnchor::class.java)
