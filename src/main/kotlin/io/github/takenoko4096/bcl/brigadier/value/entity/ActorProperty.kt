package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

sealed class ActorProperty<T>(val identifier: NamespacedIdentifier, val values: List<T>) {
    override fun hashCode(): Int {
        return identifier.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ActorProperty<*>) return false
        return identifier == other.identifier
    }

    override fun toString(): String {
        return identifier.toString()
    }

    abstract val strings: Map<T, String>

    fun toValue(value: Any?): Value<T>? {
        for (t in values) {
            if (value == t) {
                return Value(this, t)
            }
        }

        return null
    }

    @ConsistentCopyVisibility
    data class Value<T> internal constructor(val property: ActorProperty<T>, val value: T) {
        override fun toString(): String {
            return "$property=$value"
        }
    }

    companion object {
        fun boolean(identifier: NamespacedIdentifier): BooleanProperty {
            return BooleanProperty(identifier)
        }

        fun integer(identifier: NamespacedIdentifier, range: IntRange): IntegerProperty {
            return IntegerProperty(identifier, range)
        }

        fun enum(identifier: NamespacedIdentifier, vararg entries: String): EnumProperty {
            return EnumProperty(identifier, entries.toList())
        }
    }

    class BooleanProperty(identifier: NamespacedIdentifier) : ActorProperty<Boolean>(identifier, listOf(true, false)) {
        override val strings: Map<Boolean, String> = values.associateWith { it.toString() }
    }

    class IntegerProperty(identifier: NamespacedIdentifier, range: IntRange) : ActorProperty<Int>(identifier, range.toList()) {
        override val strings: Map<Int, String> = values.associateWith { it.toString() }
    }

    class EnumProperty(identifier: NamespacedIdentifier, entries: List<String>) : ActorProperty<String>(identifier, entries) {
        override val strings: Map<String, String> = values.associateWith { "\"$it\"" }
    }
}
