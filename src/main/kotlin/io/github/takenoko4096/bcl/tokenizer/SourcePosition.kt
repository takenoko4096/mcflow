package io.github.takenoko4096.bcl.tokenizer

import org.eclipse.lsp4j.Position

data class SourcePosition(val line: Int, val column: Int, val index: Int) {
    operator fun rangeTo(end: SourcePosition): SourceSpan {
        return SourceSpan(this, end)
    }

    operator fun plus(other: SourcePosition): SourcePosition {
        return SourcePosition(
            line + other.line,
            column + other.column,
            index + other.index
        )
    }

    operator fun minus(other: SourcePosition): SourcePosition {
        return SourcePosition(
            line - other.line,
            column - other.column,
            index - other.index
        )
    }

    override fun hashCode(): Int {
        return index
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is SourcePosition) false
        else index == other.index
    }

    override fun toString(): String {
        return "line ${line + 1}, column ${column + 1}"
    }

    fun toLsp4jPosition(): Position {
        return Position(line, column)
    }

    companion object {
        val ZERO = SourcePosition(0, 0, 0)

        val ONE = SourcePosition(0, 1, 1)

        fun from(sourceCode: String, position: Position): SourcePosition {
            var cursor = 0
            var currentChar: Char
            var line = 0
            var linebreakCursor = 0

            while (!(line == position.line && (cursor - linebreakCursor) == position.character) && cursor < sourceCode.length) {
                currentChar = sourceCode[cursor]
                cursor++

                if (currentChar == KeyChar.LINEBREAK) {
                    line++
                    linebreakCursor = cursor
                }
            }

            return SourcePosition(line, cursor - linebreakCursor, cursor)
        }

        fun toSourcePosition(string: String, index: Int): SourcePosition? {
            if (index !in 0..string.length) return null

            var line = 0
            var linebreakCursor = 0

            for (cursor in 0 until index) {
                if (string[cursor] == KeyChar.LINEBREAK) {
                    line++
                    linebreakCursor = cursor + 1
                }
            }

            return SourcePosition(line, index - linebreakCursor, index)
        }
    }
}
