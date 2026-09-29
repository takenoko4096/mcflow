package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class IntegerArgumentType(
    private val allowExtendedNotation: Boolean,
    private val range: IntRange?
) : ArgumentType<Int>("integer", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<Int> {
        val start = reader.getCurrentTokenStartPosition()
        val i = reader.readInteger(allowExtendedNotation)
        val end = reader.getPreviousTokenEndPosition()

        if (range != null && i !in range) {
            reader.diagnostic(start..end, "整数の読み取りに失敗しました: この引数は $range の範囲内でなければなりませんが、 $i が渡されました")
            return NoValuedArgument(this, start..end, diagnostics)
        }

        return Argument(this, i, start..end)
    }

    companion object {
        fun integer() = IntegerArgumentType(true, null)

        fun integer(range: IntRange) = IntegerArgumentType(false, range)

        fun min(min: Int) = IntegerArgumentType(true, min..Int.MAX_VALUE)

        fun nonnegative() = min(0)

        fun positive() = min(1)

        fun radixUnlimited() = IntegerArgumentType(true, null)
    }
}
