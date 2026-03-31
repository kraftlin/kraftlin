package io.github.kraftlin.command.paper.arguments

import io.github.kraftlin.command.argument
import io.github.kraftlin.command.paper.PaperArgumentNode
import io.github.kraftlin.command.paper.PaperContext
import io.github.kraftlin.command.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey

/** Adds an Adventure [Key] argument. @see [PaperContext.key] */
public fun PaperLiteralNode.key(
    name: String,
    block: PaperArgumentNode<Key>.() -> Unit,
): Unit = argument(name, ArgumentTypes.key(), block)

/** Returns the parsed [Key]. @see [PaperLiteralNode.key] */
public fun PaperContext.key(name: String): Key = rawContext.getArgument<Key>(name, Key::class.java)


/** Adds a [NamespacedKey] argument. @see [PaperContext.namespacedKey] */
public fun PaperLiteralNode.namespacedKey(
    name: String,
    block: PaperArgumentNode<NamespacedKey>.() -> Unit,
): Unit = argument(name, ArgumentTypes.namespacedKey(), block)

/** Returns the parsed [NamespacedKey]. @see [PaperLiteralNode.namespacedKey] */
public fun PaperContext.namespacedKey(name: String): NamespacedKey =
    rawContext.getArgument<NamespacedKey>(name, NamespacedKey::class.java)


/** Adds a registry resource argument that resolves to the actual registry entry of type [T]. @see [PaperContext.resource] */
public fun <T : Any> PaperLiteralNode.resource(
    name: String,
    registryKey: RegistryKey<T>,
    block: PaperArgumentNode<T>.() -> Unit,
): Unit = argument(name, ArgumentTypes.resource(registryKey), block)

/** Returns the resolved registry entry of type [T]. @see [PaperLiteralNode.resource] */
public inline fun <reified T : Any> PaperContext.resource(name: String): T =
    rawContext.getArgument<T>(name, T::class.java)


/** Adds a registry resource key argument that resolves to a [TypedKey] without loading the entry. @see [PaperContext.resourceKey] */
public fun <T : Any> PaperLiteralNode.resourceKey(
    name: String,
    registryKey: RegistryKey<T>,
    block: PaperArgumentNode<TypedKey<T>>.() -> Unit,
): Unit = argument(name, ArgumentTypes.resourceKey(registryKey), block)

/**
 * Returns the parsed [TypedKey] for a resource.
 *
 * @param T expected registry type
 * @throws ClassCastException if the key does not refer to a [T] (not checked at runtime).
 */
@Suppress("UNCHECKED_CAST")
public fun <T : Any> PaperContext.resourceKey(name: String): TypedKey<T> =
    rawContext.getArgument(name, TypedKey::class.java) as TypedKey<T>
