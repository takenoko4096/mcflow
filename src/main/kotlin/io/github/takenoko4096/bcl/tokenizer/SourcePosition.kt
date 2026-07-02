package io.github.takenoko4096.bcl.tokenizer

data class SourcePosition(val line: Int, val column: Int) {
    operator fun rangeTo(end: SourcePosition): SourceSpan {
        return SourceSpan(this, end)
    }

    override fun toString(): String {
        return "line $line, column $column"
    }
}
