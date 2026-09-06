package io.github.takenoko4096.bcl.brigadier.value

import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

sealed class BlockProperty<T>(val name: String, val values: List<T>) {
    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BlockProperty<*>) return false
        return name == other.name
    }

    override fun toString(): String {
        return name
    }

    abstract fun parse(string: String): Value<T>?

    @ConsistentCopyVisibility
    data class Value<T> internal constructor(val property: BlockProperty<T>, val value: T) {
        override fun toString(): String {
            return "$property=$value"
        }
    }

    companion object {
        fun boolean(name: String): BooleanProperty {
            return BooleanProperty(name)
        }

        fun integer(name: String, range: IntRange): IntegerProperty {
            return IntegerProperty(name, range)
        }

        inline fun <reified E> enum(name: String): EnumProperty<E> where E : Enum<E>, E : StringRepresentable {
            return EnumProperty(name, enumEntries<E>())
        }
    }

    class BooleanProperty(name: String) : BlockProperty<Boolean>(name, listOf(true, false)) {
        override fun parse(string: String) = when (string) {
            "true" -> true
            "false" -> false
            else -> null
        }?.let { Value(this, it) }
    }

    class IntegerProperty(name: String, range: IntRange) : BlockProperty<Int>(name, range.toList()) {
        override fun parse(string: String) = try {
            val i = string.toInt()
            if (i in values) i else null
        }
        catch (_: NumberFormatException) {
            null
        }?.let { Value(this, it) }
    }

    class EnumProperty<E>(name: String, entries: EnumEntries<E>) : BlockProperty<E>(name, entries) where E : Enum<E>, E : StringRepresentable {
        override fun parse(string: String) = values
            .find { it.getString() == string }
            ?.let { Value(this, it) }
    }
}
