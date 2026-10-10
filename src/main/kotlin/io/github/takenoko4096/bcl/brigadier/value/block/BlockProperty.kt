package io.github.takenoko4096.bcl.brigadier.value.block

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType
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

    /**
     * @throws IllegalArgumentException
     */
    @Deprecated(message = "use ::strings")
    abstract fun parse(token: Token): Value<T>

    abstract val strings: Map<T, String>

    fun toValue(value: Any?): Value<T>? {
        for ((entry, string) in strings) {
            val s = if (string.startsWith('"') && string.endsWith('"')) {
                string.slice(1..<string.lastIndex)
            }
            else string

            if (s == value.toString()) return Value(this, entry)
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

        override fun parse(token: Token): Value<Boolean> {
            if (token.type != TokenType.WORD) {
                throw IllegalArgumentException("プロパティ $name に渡された型 ${token.type} が正しくありません: 期待される型は真偽値です")
            }

            val f = when (token.value) {
                "true" -> true
                "false" -> false
                else -> throw IllegalArgumentException("プロパティ $name に渡された値 ${token.value} が正しくありません: 期待される値は ${values.joinToString(" | ")} です")
            }

            return Value(this, f)
        }
    }

    class IntegerProperty(name: String, range: IntRange) : BlockProperty<Int>(name, range.toList()) {
        override val strings: Map<Int, String> = values.associateWith { it.toString() }

        override fun parse(token: Token): Value<Int> {
            if (token.type != TokenType.INT) {
                throw IllegalArgumentException("プロパティ $name に渡された型 ${token.type} が正しくありません: 期待される型は整数です")
            }

            val i = token.value.toInt()

            if (i !in values) {
                throw IllegalArgumentException("プロパティ $name に渡された値 ${token.value} が正しくありません: 期待される値は ${values.joinToString(" | ")} です")
            }

            return Value(this, i)
        }
    }

    class EnumProperty<E>(name: String, entries: EnumEntries<E>) : BlockProperty<E>(name, entries) where E : Enum<E>, E : StringRepresentable {
        override val strings: Map<E, String> = values.associateWith { "\"${it.getString()}\"" }

        override fun parse(token: Token): Value<E> {
            if (token.type != TokenType.DOUBLE_QUOTED) {
                throw IllegalArgumentException("プロパティ $name に渡された型 ${token.type} が正しくありません: 期待される型は文字列です")
            }

            val value = values.find { it.getString() == token.value }

            if (value == null) {
                throw IllegalArgumentException("プロパティ $name に渡された値 ${token.value} が正しくありません: 期待される値は ${values.joinToString(" | ")} です")
            }

            return Value(this, value)
        }
    }
}
