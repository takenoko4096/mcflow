package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.location.Axis
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class AxesArgumentType : ArgumentType<Set<Axis>>("axes") {
    override fun parse(reader: TokenSeqReader): ArgumentParseResult<Set<Axis>> {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()
        val start = reader.getCurrentTokenStartPosition()
        val string = reader.readCommandStringAsFatal()
        val end = reader.getPreviousTokenEndPosition()
        val sourceSpan = start..end

        val set = string.toCharArray()
            .map { char ->
                Axis.entries.find { it.getString() == char.toString() }
                    ?: run {
                        diagnostics.add(reader.diagnostic(sourceSpan, "軸の読み取りに失敗しました: 軸名 '$char' は無効です"))
                        null
                    }
            }
            .filterNotNull()

        val axes = mutableSetOf<Axis>()
        for (element in set) {
            if (element in axes) {
                diagnostics.add(reader.diagnostic(sourceSpan, "軸の読み取りに失敗しました: 軸 '$element' が重複しています"))
            }

            axes.add(element)
        }

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.AXES, sourceSpan))

        if (diagnostics.isNotEmpty()) {
            return ArgumentParseResult.Failure(this, sourceSpan, diagnostics, semanticTokens)
        }

        return Argument(this, axes, sourceSpan, semanticTokens)
    }
}
