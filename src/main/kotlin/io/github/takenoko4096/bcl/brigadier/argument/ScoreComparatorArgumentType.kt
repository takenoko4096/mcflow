package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.misc.ScoreComparator
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class ScoreComparatorArgumentType : ArgumentType<ScoreComparator>("score_comparator") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<ScoreComparator> {
        val start = reader.getCurrentTokenStartPosition()

        for (comparator in ScoreComparator.entries) {
            if (reader.peek() `is` comparator.symbol) {
                reader.next()
                val end = reader.getPreviousTokenEndPosition()

                return Argument(this, comparator, start..end)
            }
        }

        reader.next()
        val end = reader.getPreviousTokenEndPosition()
        throw reader.exception(start..end, "スコア比較演算子の解析に失敗しました: 無効なトークンです: ${reader.peekSafely(-1)}")
    }
}
