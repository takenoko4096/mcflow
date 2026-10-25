package io.github.takenoko4096.bcl.brigadier.value.block

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
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

    abstract val strings: Map<T, String>

    fun toValue(value: Any?): Value<T>? {
        for (t in values) {
            if (value == t) {
                return Value(this, t)
            }
            else if (t is StringRepresentable && value == t.getString()) {
                return Value(this, t)
            }
        }

        return null
    }

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
        override val strings: Map<Boolean, String> = values.associateWith { it.toString() }
    }

    class IntegerProperty(name: String, range: IntRange) : BlockProperty<Int>(name, range.toList()) {
        override val strings: Map<Int, String> = values.associateWith { it.toString() }
    }

    class EnumProperty<E>(name: String, entries: EnumEntries<E>) : BlockProperty<E>(name, entries) where E : Enum<E>, E : StringRepresentable {
        override val strings: Map<E, String> = values.associateWith { "\"${it.getString()}\"" }
    }
}
