package io.github.takenoko4096.bcl.tokenizer

data class SourceSpan(val start: SourcePosition, val end: SourcePosition) {
    init {
        if (end.index < start.index) {
            throw IllegalArgumentException("end ($end) < start ($start)")
        }
    }

    override fun toString(): String {
        return "$start to $end"
    }
}
