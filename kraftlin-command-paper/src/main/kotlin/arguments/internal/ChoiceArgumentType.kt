package io.github.kbrigx.paper.arguments.internal

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.Message
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.util.concurrent.CompletableFuture


internal class ChoiceArgumentType<T : Any> private constructor(
    private val byToken: Map<String, T>,
    private val tokensInDisplayOrder: List<String>,
    private val errorMessage: (input: String, allowedKeys: List<String>) -> Message,
) : CustomArgumentType.Converted<T, String> {

    override fun getNativeType(): ArgumentType<String> = StringArgumentType.word()

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>, builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        // Brigadier will handle prefix filtering based on the builder's remaining input.
        for (k in tokensInDisplayOrder) {
            builder.suggest(k)
        }
        return builder.buildFuture()
    }

    override fun convert(nativeType: String): T {
        val value = byToken[nativeType]
        if (value != null) return value

        val msg = errorMessage(nativeType, tokensInDisplayOrder)

        throw SimpleCommandExceptionType(msg).create()
    }

    internal companion object {

        internal fun <T : Any> of(
            values: Iterable<T>,
            token: (T) -> String,
            errorMessage: (input: String, allowedKeys: List<String>) -> Message = { input, allowed ->
                LiteralMessage("Invalid value '$input'. Expected one of: ${allowed.joinToString(", ")}")
            }
        ): ChoiceArgumentType<T> {
            val map = LinkedHashMap<String, T>()
            val display = ArrayList<String>()

            for (v in values) {
                val rawKey = token(v)
                require(rawKey.isNotBlank()) { "Choice key must not be blank (value=$v)" }
                require(!rawKey.any { it.isWhitespace() }) {
                    "Choice key must be a single token without whitespace: '$rawKey' (value=$v)"
                }

                val prev = map.putIfAbsent(rawKey, v)
                require(prev == null) {
                    "Duplicate choice key '$rawKey'. Values: $prev and $v"
                }

                display += rawKey
            }

            return ChoiceArgumentType(map, display, errorMessage)
        }
    }
}


@PublishedApi
internal fun <E : Enum<E>> enumChoice(values: Iterable<E>): ArgumentType<E> =
    ChoiceArgumentType.of(values, { e -> e.name.lowercase() })

internal fun stringChoice(values: Iterable<String>): ArgumentType<String> =
    ChoiceArgumentType.of(values, { it })
