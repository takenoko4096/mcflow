package io.github.takenoko4096.bcl.tokenizer

data class SourceSpan(val start: SourcePosition, val end: SourcePosition) {
    init {
        if (end.index < start.index) {
            throw IllegalArgumentException("end ($end) < start ($start)")
        }
    }

    override fun toString(): String {
        if (this == NULL) return "NULL_SOURCE_SPAN"
        return if (start == end) start.toString() else "$start to $end"
    }

    fun getStringFromSourceCode(sourceCode: String) = sourceCode.slice(start.index..end.index)

    companion object {
        val NULL = SourcePosition.NULL..SourcePosition.NULL
    }
}
