package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol

class IntegerArgumentType(
    private val signedDecimalOnly: Boolean,
    private val validator: (TokenSeqReader.(Int, SourceSpan) -> Unit)?
) : ArgumentType<Int>("integer", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): Argument<Int> {
        val start = reader.getCurrentTokenStartPosition()
        val i = reader.readInteger(signedDecimalOnly)
        val end = reader.getPreviousTokenEndPosition()

        validator?.invoke(reader, i, start..end)

        return Argument(this, i, start..end)
    }

    companion object {
        fun integer() = IntegerArgumentType(true, null)

        fun integer(range: ClosedRange<Int>) = IntegerArgumentType(true) { value, sourceSpan ->
            if (value !in range) {
                throw exception(sourceSpan, "整数の読み取りに失敗しました: この引数は $range の範囲内でなければなりませんが、 $value が渡されました")
            }
        }

        fun min(min: Int) = IntegerArgumentType(true) { value, sourceSpan ->
            if (value < min) {
                throw exception(sourceSpan, "整数の読み取りに失敗しました: この引数は $min.. の範囲内でなければなりませんが、 $value が渡されました")
            }
        }

        fun nonnegative() = min(0)

        fun positive() = min(1)

        fun radixUnlimited() = IntegerArgumentType(false, null)
    }
}
