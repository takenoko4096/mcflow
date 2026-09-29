package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class BooleanArgumentType : ArgumentType<Boolean>("boolean", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<Boolean> {
        val start = reader.getCurrentTokenStartPosition()
        val bool = reader.unfatalize(diagnostics) { readBool() }
        val end = reader.getPreviousTokenEndPosition()

        if (bool is TokenSeqReader.ReadResult.Failure) {
            return NoValuedArgument(this, start..end, diagnostics)
        }

        return Argument(this, (bool as TokenSeqReader.ReadResult.Successful).value, start..end)
    }
}
