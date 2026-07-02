package io.github.takenoko4096.bcl.tokenizer

import io.github.takenoko4096.bcl.BclException
import kotlin.math.max
import kotlin.math.min

class BclTokenizationException internal constructor(
    message: String,
    script: String,
    cursor: Int,
    position: SourcePosition,
    cause: Throwable?
) : BclException("$message @line ${position.line}, column ${position.column}: ${createPointer(script, cursor)}", cause) {
    companion object {
        private fun createPointer(script: String, cursor: Int): String {
            val range = 16

            val start = max(0, cursor - range)
            val end = min(script.length - 1, cursor + range)
            val part = script.slice(start..end)
            val localCursor = (cursor - start).coerceIn(0..<part.length)
            val firstHalf = part.slice(0..<localCursor)
            val secondHalf = part.slice((localCursor + 1)..<part.length)
            return firstHalf + ">>>" + part[localCursor] + "<<<" + secondHalf
        }
    }
}
