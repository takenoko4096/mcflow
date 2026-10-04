package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.entity.ExperienceAmount
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class ExperienceAmountArgumentType : ArgumentType<ExperienceAmount>("experience_amount") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<ExperienceAmount> {
        val start = reader.getCurrentTokenStartPosition()
        val amount = reader.readExperienceAmount()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (!amount.isLevel && amount.value < 0) {
            diagnostics.add(reader.diagnostic(start..end, "経験値量の読み取りに失敗しました: 負の経験値量は無効です (負のレベルを意図したものでしたか？) : ${amount.value}"))
            return NoValuedArgument(this, start..end, diagnostics)
        }

        return Argument(this, amount, start..end)
    }
}
