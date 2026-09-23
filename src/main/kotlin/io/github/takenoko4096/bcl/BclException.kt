package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

abstract class BclException protected constructor(
    val description: String,
    sourceCode: String,
    val sourceSpan: SourceSpan,
    cause: Throwable?
) : RuntimeException(getPointedMessage(description, sourceCode, sourceSpan), cause) {
    companion object {
        const val ERROR_POINTER_SPAN_SIZE = 48

        fun getPointedMessage(message: String, sourceCode: String, sourceSpan: SourceSpan): String {
            val i0 = sourceSpan.start.index
            val i1 = sourceSpan.end.index

            if (sourceCode.lastIndex == -1) {
                return "$message @$sourceSpan; >> <<"
            }

            val startA = ((i0 - 1) - ERROR_POINTER_SPAN_SIZE).coerceIn(0..sourceCode.lastIndex)
            val endA = (i0 - 1).let {
                if (it in 0..sourceCode.lastIndex) it else null
            }

            val startB = (i1 + 1).let {
                if (it in 0..sourceCode.lastIndex) it else null
            }
            val endB = ((i1 + 1) + ERROR_POINTER_SPAN_SIZE).coerceIn(0..sourceCode.lastIndex)

            val a = if (endA != null) {
                if (startA < 0 || endA > sourceCode.lastIndex) ""
                else sourceCode.slice(startA..endA)
                    .replace(KeyChar.LINEBREAK, ';')
                    .replace("\\s+".toRegex(), KeyChar.SPACE.toString())
            } else ""

            val m = if (i0 < 0 || i1 > sourceCode.lastIndex) "" else sourceCode.slice(i0..i1)
                .replace(KeyChar.LINEBREAK, ';')
                .replace("\\s+".toRegex(), KeyChar.SPACE.toString())

            val b = if (startB != null) {
                if (endB > sourceCode.lastIndex) ""
                else sourceCode.slice(startB..endB)
                    .replace(KeyChar.LINEBREAK, ';')
                    .replace("\\s+".toRegex(), KeyChar.SPACE.toString())
            } else ""

            val halfWidth = "[\\x20-\\x7E\\uFF61-\\uFF9F]".toRegex()
            val fullWidth = "[^\\x20-\\x7E\\uFF61-\\uFF9F]".toRegex()

            val space = a.replace(halfWidth, " ").replace(fullWidth, "　")
            val pointer = m.replace(halfWidth, "^").replace(fullWidth, "＾")

            val area = "$a$m$b" + '\n' + space + pointer

            return "$message @$sourceSpan; \n$area"
        }
    }
}
