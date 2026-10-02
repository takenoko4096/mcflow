package io.github.takenoko4096.bcl.tokenizer

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import java.util.Objects

data class SourceSpan(val start: SourcePosition, val end: SourcePosition) {
    init {
        if (end.index < start.index) {
            throw IllegalArgumentException("end ($end) < start ($start)")
        }
    }

    fun trimEdge(): SourceSpan {
        return SourcePosition(start.line, start.column + 1, start.index + 1)..SourcePosition(end.line, end.column - 1, end.index - 1)
    }

    override fun hashCode(): Int {
        return Objects.hash(start, end)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is SourceSpan) false
        else start == other.start && end == other.end
    }

    override fun toString(): String {
        if (this == NULL) return "NULL_SOURCE_SPAN"
        return if (start == end) start.toString() else "$start to $end"
    }

    fun toLsp4jRange(): Range? {
        val startPosition = start.toLsp4jPosition()
        val endPositionInclusive = end.toLsp4jPosition()
        if (startPosition == null || endPositionInclusive == null) return null
        val endPositionExclusive = Position(endPositionInclusive.line, endPositionInclusive.character + 1)
        return Range(startPosition, endPositionExclusive)
    }

    fun getStringFromSourceCode(sourceCode: String) = sourceCode.slice(start.index..end.index)

    companion object {
        val NULL = SourcePosition.NULL..SourcePosition.NULL
    }
}
