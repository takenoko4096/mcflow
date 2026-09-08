package io.github.takenoko4096.bcl.brigadier.value.entity

class EntitySelector(val type: SelectorType, val arguments: SelectorArguments) {
    private val c = if (type.argumentsOverridable) arguments.c ?: type.defaultArguments.c else type.defaultArguments.c

    val isSingle = c == 1 || c == -1

    val isSortOrderInverted = c != null && c < 0
}
