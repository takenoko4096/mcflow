package io.github.takenoko4096.bcl.brigadier.value.misc

data class InvertibleIntegerRange(val inverted: Boolean, val ints: IntRange) {
    constructor(inverted: Boolean = false, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE) : this(inverted, min..max)

    fun toFullString() = "${ints.first}..${ints.last}"

    override fun toString(): String {
        return if (ints.first == ints.last) {
            ints.first.toString()
        }
        else if (ints.first == Int.MIN_VALUE && ints.last != Int.MAX_VALUE) {
            ".." + ints.last.toString()
        }
        else if (ints.first != Int.MIN_VALUE && ints.last == Int.MAX_VALUE) {
            ints.first.toString() + ".."
        }
        else {
            toFullString()
        }
    }

    companion object {
        val ZERO = InvertibleIntegerRange(min = 0, max = 0)
    }
}
