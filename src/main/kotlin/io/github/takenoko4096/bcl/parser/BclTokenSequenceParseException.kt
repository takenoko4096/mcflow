package io.github.takenoko4096.bcl.parser

import io.github.takenoko4096.bcl.BclException
import io.github.takenoko4096.bcl.tokenizer.Token
import kotlin.math.max
import kotlin.math.min

class BclTokenSequenceParseException internal constructor(
    message: String,
    tokens: List<Token>,
    token: Token,
    cursor: Int,
    cause: Throwable?
) : BclException("$message @${token.sourceSpan}: ${createPointer(tokens, cursor)}", cause) {
    companion object {
        private fun createPointer(tokens: List<Token>, cursor: Int): String {
            val range = 16

            val start = max(0, cursor - range)
            val end = min(tokens.size - 1, cursor + range)
            val part = tokens.slice(start..end)
            val localCursor = (cursor - start).coerceIn(0..<part.size)
            val firstHalf = part.slice(0..<localCursor).joinToString(" ", transform = Token::value)
            val secondHalf = part.slice((localCursor + 1)..<part.size).joinToString(" ", transform = Token::value)
            return firstHalf + " >>> " + part[localCursor].value + " <<< " + secondHalf
        }
    }
}
