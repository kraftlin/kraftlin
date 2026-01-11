package io.github.kbrigx.paper.arguments

import io.github.kbrigx.core.argument
import io.github.kbrigx.paper.PaperArgumentNode
import io.github.kbrigx.paper.PaperContext
import io.github.kbrigx.paper.PaperLiteralNode
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.registry.RegistryKey
import io.papermc.paper.registry.TypedKey
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey

public fun PaperLiteralNode.key(
    name: String,
    block: PaperArgumentNode<Key>.() -> Unit,
): Unit = argument(name, ArgumentTypes.key(), block)

public fun PaperContext.key(name: String): Key = rawContext.getArgument<Key>(name, Key::class.java)


public fun PaperLiteralNode.namespacedKey(
    name: String,
    block: PaperArgumentNode<NamespacedKey>.() -> Unit,
): Unit = argument(name, ArgumentTypes.namespacedKey(), block)

public fun PaperContext.namespacedKey(name: String): NamespacedKey =
    rawContext.getArgument<NamespacedKey>(name, NamespacedKey::class.java)


public fun <T : Any> PaperLiteralNode.resource(
    name: String,
    registryKey: RegistryKey<T>,
    block: PaperArgumentNode<T>.() -> Unit,
): Unit = argument(name, ArgumentTypes.resource(registryKey), block)

public inline fun <reified T : Any> PaperContext.resource(name: String): T =
    rawContext.getArgument<T>(name, T::class.java)


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
