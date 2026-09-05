package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

abstract class BclException protected constructor(
    val description: String,
    text: String,
    val span: SourceSpan,
    cause: Throwable?
) : RuntimeException(getPointedMessage(description, text, span), cause) {
    companion object {
        const val ERROR_POINTER_SPAN_SIZE = 48

        fun getPointedMessage(message: String, text: String, span: SourceSpan): String {
            val i0 = span.start.index
            val i1 = span.end.index

            val startA = ((i0 - 1) - ERROR_POINTER_SPAN_SIZE).coerceIn(0..text.lastIndex)
            val endA = (i0 - 1).coerceIn(0..text.lastIndex)

            val startB = (i1 + 1).coerceIn(0..text.lastIndex)
            val endB = ((i1 + 1) + ERROR_POINTER_SPAN_SIZE).coerceIn(0..text.lastIndex)

            val a = text.slice(startA..endA)
                .replace(KeyChar.LINEBREAK, ';')
                .replace("\\s+".toRegex(), KeyChar.SPACE.toString())

            val m = text.slice(i0..i1)
                .replace(KeyChar.LINEBREAK, ';')
                .replace("\\s+".toRegex(), KeyChar.SPACE.toString())

            val b = text.slice(startB..endB)
                .replace(KeyChar.LINEBREAK, ';')
                .replace("\\s+".toRegex(), KeyChar.SPACE.toString())

            val area = "$a$m$b\n" + " ".repeat(a.length) + "^".repeat(m.length)

            return "$message @$span; \n$area"
        }
    }
}
