package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.misc.TimeDuration
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class TimeDurationArgumentType : ArgumentType<TimeDuration>("time_duration", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): Argument<TimeDuration> {
        val start = reader.getCurrentTokenStartPosition()
        val duration = reader.readTimeDuration()
        val end = reader.getPreviousTokenEndPosition()
        return Argument(this, duration, start..end)
    }
}
