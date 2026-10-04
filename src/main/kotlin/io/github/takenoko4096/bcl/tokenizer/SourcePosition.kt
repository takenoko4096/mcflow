package io.github.takenoko4096.bcl.tokenizer

import org.eclipse.lsp4j.Position

data class SourcePosition(val line: Int, val column: Int, val index: Int) {
    operator fun rangeTo(end: SourcePosition): SourceSpan {
        return SourceSpan(this, end)
    }

    operator fun plus(other: SourcePosition): SourcePosition {
        return SourcePosition(line + other.line - 1, column + other.column, index + other.index)
    }

    operator fun minus(other: SourcePosition): SourcePosition {
        return SourcePosition(line - other.line - 1, column - other.column, index - other.index)
    }

    override fun hashCode(): Int {
        return index
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is SourcePosition) false
        else index == other.index
    }

    override fun toString(): String {
        if (this == NULL) return "NULL_SOURCE_POSITION"
        return "line $line, column ${column + 1}"
    }

    fun toLsp4jPosition(): Position? {
        if (this === NULL) return null
        return Position(line - 1, column)
    }

    companion object {
        val NULL = SourcePosition(0, 0, -1)

        val OFFSET_NEXT_COLUMN = SourcePosition(1, 1, 1)
    }
}
