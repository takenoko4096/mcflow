package io.github.takenoko4096.bcl.brigadier.value.entity

class InvertibleValue<T>(val inverted: Boolean, val value: T) {
    override fun toString(): String {
        return if (inverted) "!$value" else value.toString()
    }

    companion object {
        fun <T> inverted(value: T) = InvertibleValue(true, value)

        fun <T> uninverted(value: T) = InvertibleValue(false, value)
    }
}