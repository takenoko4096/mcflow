package io.github.takenoko4096.bcl.tokenizer

import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import java.util.Objects

data class SourceSpan(val start: SourcePosition, val end: SourcePosition) {
    init {
        if (end.index < start.index) {
            throw IllegalArgumentException("end ${end.index} ($end) < start ${start.index} ($start)")
        }
    }

    fun trimEdge(): SourceSpan? {
        val newStart = SourcePosition(start.line, start.column + 1, start.index + 1)
        val newEnd = SourcePosition(end.line, end.column - 1, end.index - 1)
        return if (newEnd.index < newStart.index) null else newStart..newEnd
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

    operator fun contains(other: SourceSpan): Boolean {
        val range = start.index..end.index
        return other.start.index in range && other.end.index in range
    }

    operator fun contains(position: Position): Boolean {
        return if ((position.line + 1) == start.line) {
            position.character >= start.column
        }
        else if ((position.line + 1) == end.line) {
            position.character <= end.column
        }
        else if (start.line < (position.line + 1) && (position.line + 1) < end.line) {
            true
        }
        else {
            false
        }
    }

    fun exclude(exclusions: List<SourceSpan>): List<SourceSpan> {
        var remaining = listOf(this)

        for (exclusion in exclusions) {
            remaining = remaining.flatMap { span ->
                if (exclusion.end.index < span.start.index || span.end.index < exclusion.start.index) {
                    listOf(span)
                }
                else {
                    buildList {
                        if (span.start.index < exclusion.start.index) {
                            add(SourceSpan(span.start, exclusion.start - SourcePosition.OFFSET_NEXT_COLUMN))
                        }
                        if (exclusion.end.index < span.end.index) {
                            add(SourceSpan(exclusion.end + SourcePosition.OFFSET_NEXT_COLUMN, span.end))
                        }
                    }
                }
            }
        }

        return remaining
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
