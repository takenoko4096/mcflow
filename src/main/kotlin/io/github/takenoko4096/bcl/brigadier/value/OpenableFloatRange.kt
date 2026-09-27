package io.github.takenoko4096.bcl.brigadier.value

data class OpenableFloatRange(val start: Float, val end: Float, val isStartOpen: Boolean, val isEndOpen: Boolean) {
    operator fun contains(value: Float): Boolean {
        return if (isStartOpen && isEndOpen) {
            start < value && value < end
        }
        else if (isStartOpen) {
            start < value && value <= end
        }
        else if (isEndOpen) {
            value in start..<end
        }
        else {
            value in start..end
        }
    }

    override fun toString(): String {
        return "${this::class.simpleName} ${if (isStartOpen) '(' else '['}$start, $end${if (isEndOpen) ')' else ']'}"
    }

    companion object {
        fun openRange(start: Float, last: Float) = OpenableFloatRange(start, last, isStartOpen = true, isEndOpen = true)

        fun openStartRange(start: Float, last: Float) = OpenableFloatRange(start, last, isStartOpen = true, isEndOpen = false)

        fun openEndRange(start: Float, last: Float) = OpenableFloatRange(start, last, isStartOpen = false, isEndOpen = true)

        fun closedRange(start: Float, last: Float) = OpenableFloatRange(start, last, isStartOpen = false, isEndOpen = false)

        fun from(range: ClosedFloatingPointRange<Float>) = closedRange(range.start, range.endInclusive)
    }
}
