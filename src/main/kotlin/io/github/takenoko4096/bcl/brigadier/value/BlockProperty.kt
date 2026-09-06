package io.github.takenoko4096.bcl.brigadier.value

import kotlin.enums.EnumEntries

sealed class BlockProperty<T>(val identifier: NamespacedIdentifier) {
    abstract fun validate(value: T): Boolean

    override fun hashCode(): Int {
        return identifier.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BlockProperty<*>) return false
        return identifier == other.identifier
    }

    override fun toString(): String {
        return identifier.toString()
    }

    class BooleanProperty(identifier: NamespacedIdentifier) : BlockProperty<Boolean>(identifier) {
        override fun validate(value: Boolean) = true
    }

    class IntegerProperty(identifier: NamespacedIdentifier, val range: ClosedRange<Int>) : BlockProperty<Int>(identifier) {
        override fun validate(value: Int) = value in range
    }

    class EnumProperty<E>(identifier: NamespacedIdentifier, val entries: EnumEntries<E>) : BlockProperty<E>(identifier) where E : Enum<E> {
        override fun validate(value: E) = value in entries
    }
}
