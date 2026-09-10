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

            val startA = ((i0 - 1) - ERROR_POINTER_SPAN_SIZE).coerceIn(0..sourceCode.lastIndex)
            val endA = (i0 - 1).let {
                if (it in 0..sourceCode.lastIndex) it else null
            }

            val startB = (i1 + 1).let {
                if (it in 0..sourceCode.lastIndex) it else null
            }
            val endB = ((i1 + 1) + ERROR_POINTER_SPAN_SIZE).coerceIn(0..sourceCode.lastIndex)

            val a = if (endA != null) sourceCode.slice(startA..endA)
                .replace(KeyChar.LINEBREAK, ';')
                .replace("\\s+".toRegex(), KeyChar.SPACE.toString()) else ""

            val m = sourceCode.slice(i0..i1)
                .replace(KeyChar.LINEBREAK, ';')
                .replace("\\s+".toRegex(), KeyChar.SPACE.toString())

            val b = if (startB != null) sourceCode.slice(startB..endB)
                .replace(KeyChar.LINEBREAK, ';')
                .replace("\\s+".toRegex(), KeyChar.SPACE.toString()) else ""

            println(a)
            println(m)
            println(b)

            val area = "$a$m$b\n" + " ".repeat(a.length) + "^".repeat(m.length)

            return "$message @$sourceSpan; \n$area"
        }
    }
}
