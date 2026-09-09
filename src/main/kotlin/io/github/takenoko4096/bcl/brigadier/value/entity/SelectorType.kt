package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.WithSpan
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

enum class SelectorType(
    private val string: String,
    val aliveOnly: Boolean,
    val isPlayer: Boolean,
    val defaultSortOrder: SelectorSortOrder,
    val defaultArguments: SelectorArguments,
    val argumentsOverridable: Boolean
) : StringRepresentable {
    P(
        "@p",
        aliveOnly = true,
        isPlayer = true,
        defaultSortOrder = SelectorSortOrder.DISTANCE,
        defaultArguments = SelectorArguments(c = WithSpan.noSpan(1)),
        argumentsOverridable = true
    ),
    A(
        "@a",
        aliveOnly = false,
        isPlayer = true,
        defaultSortOrder = SelectorSortOrder.RANDOM,
        defaultArguments = SelectorArguments(c = WithSpan.noSpan(1)),
        argumentsOverridable = true
    ),
    R(
        "@r",
        aliveOnly = true,
        isPlayer = false,
        defaultSortOrder = SelectorSortOrder.RANDOM,
        defaultArguments = SelectorArguments(
            c = WithSpan.noSpan(1),
            type = mutableListOf(InvertibleValue.uninverted(EntityType.PLAYER, SourceSpan.NULL))
        ),
        argumentsOverridable = true
    ),
    S(
        "@s",
        aliveOnly = false,
        isPlayer = false,
        defaultSortOrder = SelectorSortOrder.DISTANCE,
        defaultArguments = SelectorArguments(c = WithSpan.noSpan(1)),
        argumentsOverridable = false
    ),
    E(
        "@e",
        aliveOnly = true,
        isPlayer = false,
        defaultSortOrder = SelectorSortOrder.DISTANCE,
        defaultArguments = SelectorArguments(),
        argumentsOverridable = true
    ),
    N(
        "@n",
        aliveOnly = true,
        isPlayer = false,
        defaultSortOrder = SelectorSortOrder.DISTANCE,
        defaultArguments = SelectorArguments(c = WithSpan.noSpan(1)),
        argumentsOverridable = true
    ),
    INITIATOR(
        "@initiator",
        aliveOnly = false,
        isPlayer = false,
        defaultSortOrder = SelectorSortOrder.DISTANCE,
        defaultArguments = SelectorArguments(),
        argumentsOverridable = true
    ),
    PLAYER(
        "(player)",
        aliveOnly = false,
        isPlayer = true,
        defaultSortOrder = SelectorSortOrder.DISTANCE,
        defaultArguments = SelectorArguments(),
        argumentsOverridable = true
    );

    override fun getString(): String {
        return string
    }
}
