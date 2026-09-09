package io.github.takenoko4096.bcl.brigadier.value.entity

class EntitySelector(val type: SelectorType, val arguments: SelectorArguments) {
    private val c = if (type.argumentsOverridable) arguments.c ?: type.defaultArguments.c else type.defaultArguments.c

    val isSingle = c?.value == 1 || c?.value == -1

    val isSortOrderInverted = c?.value != null && c.value < 0

    override fun toString(): String {
        return "${type.getString()}[$arguments]"
    }
}
