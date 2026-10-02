package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.Symbol

class WildcardIntegerArgumentType : ArgumentType<Int?>("wildcard_integer", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): Argument<Int?> {
        val start = reader.getCurrentTokenStartPosition()

        val i = if (reader.peek() `is` Symbol.MULTIPLY) {
            reader.next()
            null
        }
        else {
            reader.readInteger(allowExtendedNotation = false)
        }

        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        return Argument(this, i, start..end)
    }
}
