package io.github.takenoko4096.bcl.brigadier.value.entity

class EntitySelector(val type: SelectorType, val arguments: SelectorArguments) {
    val isSingle: Boolean

    init {
        isSingle = if (type.argumentsOverridable) {
            arguments.c ?: type.defaultArguments.c
        }
        else {
            type.defaultArguments.c
        } == 1
    }
}
