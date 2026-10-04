package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.misc.TimeDuration
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class TimeDurationArgumentType : ArgumentType<TimeDuration>("time_duration", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<TimeDuration> {
        val start = reader.getCurrentTokenStartPosition()
        val duration = reader.readTimeDuration()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (duration.value < 0) {
            diagnostics.add(reader.diagnostic(start..end, "時間の読み取りに失敗しました: 負の時間は無効です: '$duration'"))
            return NoValuedArgument(this, start..end, diagnostics)
        }

        return Argument(this, duration, start..end)
    }
}
