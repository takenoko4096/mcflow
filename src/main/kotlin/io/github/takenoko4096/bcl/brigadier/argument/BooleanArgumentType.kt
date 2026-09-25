package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class BooleanArgumentType : ArgumentType<Boolean>("boolean", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): Argument<Boolean> {
        val start = reader.getCurrentTokenStartPosition()
        val bool = reader.readBool()
        val end = reader.getPreviousTokenEndPosition()
        return Argument(this, bool, start..end)
    }
}
