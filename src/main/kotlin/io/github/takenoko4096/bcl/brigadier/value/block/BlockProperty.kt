package io.github.takenoko4096.bcl.brigadier.value.block

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import kotlin.enums.enumEntries

sealed class BlockProperty<T>(val name: String, val values: List<T>, val isVanilla: Boolean) {
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
        fun boolean(name: String, isVanilla: Boolean = true): BooleanProperty {
            return BooleanProperty(name, isVanilla)
        }

        fun integer(name: String, range: IntRange, isVanilla: Boolean = true): IntegerProperty {
            return IntegerProperty(name, range, isVanilla)
        }

        inline fun <reified E> enum(name: String, isVanilla: Boolean = true): EnumProperty where E : Enum<E>, E : StringRepresentable {
            return EnumProperty(name, enumEntries<E>().map { it.getString() }, isVanilla)
        }

        fun unvanilla(name: String, value: Any): BlockProperty<*> = when (value) {
            is Boolean -> BooleanProperty(name, isVanilla = false)
            is Int -> IntegerProperty(name, value..value, isVanilla = false)
            is String -> EnumProperty(name, listOf(value), isVanilla = false)
            else -> throw IllegalArgumentException()
        }
    }

    class BooleanProperty(name: String, isVanilla: Boolean) : BlockProperty<Boolean>(name, listOf(true, false), isVanilla) {
        override val strings: Map<Boolean, String> = values.associateWith { it.toString() }
    }

    class IntegerProperty(name: String, range: IntRange, isVanilla: Boolean) : BlockProperty<Int>(name, range.toList(), isVanilla) {
        override val strings: Map<Int, String> = values.associateWith { it.toString() }
    }

    class EnumProperty(name: String, entries: List<String>, isVanilla: Boolean) : BlockProperty<String>(name, entries, isVanilla) {
        override val strings: Map<String, String> = values.associateWith { it }
    }
}
