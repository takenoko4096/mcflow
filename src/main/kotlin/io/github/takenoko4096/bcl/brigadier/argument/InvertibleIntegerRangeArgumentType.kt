package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.misc.InvertibleIntegerRange
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.Symbol

class InvertibleIntegerRangeArgumentType : ArgumentType<InvertibleIntegerRange>("integer_range") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<InvertibleIntegerRange> {
        val start = reader.getCurrentTokenStartPosition()

        val inv = if (reader.peek() `is` Symbol.LOGICAL_NOT) {
            reader.next()
            true
        }
        else false

        val range = reader.readIntRange()

        val end = reader.getPreviousTokenEndPosition()

        return Argument(this, InvertibleIntegerRange(inv, range), start..end)
    }
}
