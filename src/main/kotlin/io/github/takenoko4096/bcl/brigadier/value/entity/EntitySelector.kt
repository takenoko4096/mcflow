package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.tokenizer.KeyChar

class EntitySelector(val type: SelectorType, val arguments: SelectorArguments) {
    private val c = if (type.argumentsOverridable) arguments.c ?: type.defaultArguments.c else type.defaultArguments.c

    val isPlayer = type.isPlayer || arguments.type.any { !it.inverted && it.value == EntityType.PLAYER }

    val isSingle = c?.value == 1 || c?.value == -1

    val isInvertedOrder = c?.value != null && c.value < 0

    val isWildcard = type == SelectorType.PLAYER && arguments.name.size == 1 && !arguments.name.first().inverted && arguments.name.first().value == KeyChar.WILDCARD.toString()

    override fun toString(): String {
        return "${type.getString()}${arguments.let { if (it.isEmpty()) "" else it }}"
    }
}
