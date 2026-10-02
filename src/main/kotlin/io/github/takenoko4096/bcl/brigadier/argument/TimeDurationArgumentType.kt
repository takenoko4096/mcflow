package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.misc.TimeDuration
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class TimeDurationArgumentType : ArgumentType<TimeDuration>("time_duration", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<TimeDuration> {
        val start = reader.getCurrentTokenStartPosition()
        val duration = reader.readTimeDuration()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        return Argument(this, duration, start..end)
    }
}
