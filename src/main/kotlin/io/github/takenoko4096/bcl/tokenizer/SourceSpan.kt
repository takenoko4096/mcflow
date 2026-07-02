package io.github.takenoko4096.bcl.tokenizer

data class SourceSpan(val start: SourcePosition, val end: SourcePosition) {
    override fun toString(): String {
        return "$start to $end"
    }
}
