package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.misc.ScoreOperator
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class ScoreOperatorArgumentType : ArgumentType<ScoreOperator>("score_operator") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<ScoreOperator> {
        val start = reader.getCurrentTokenStartPosition()

        for (operator in ScoreOperator.entries) {
            if (reader.peek() `is` operator.symbol) {
                reader.next()
                val end = reader.getPreviousTokenEndPosition()

                return Argument(this, operator, start..end)
            }
        }

        reader.next()
        val end = reader.getPreviousTokenEndPosition()
        throw reader.exception(start..end, "スコア演算子の解析に失敗しました: 無効なトークンです: ${reader.peekSafely(-1)}")
    }
}
