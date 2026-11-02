package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class IntegerArgumentType(
    private val allowExtendedNotation: Boolean,
    private val range: IntRange?
) : ArgumentType<Int>("integer", hasInlayHint = true) {
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<Int> {
        val reader = context.reader
        val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

        val start = reader.getCurrentTokenStartPosition()
        val i = reader.readInteger(allowExtendedNotation)
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (range != null && i !in range) {
            reader.diagnostic(start..end, "整数の読み取りに失敗しました: この引数は $range の範囲内でなければなりませんが、 $i が渡されました")
            return NoValuedArgument(this, start..end, diagnostics)
        }

        return Argument(this, i, start..end)
    }

    companion object {
        fun integer() = IntegerArgumentType(false, null)

        fun integer(range: IntRange) = IntegerArgumentType(false, range)

        fun min(min: Int) = IntegerArgumentType(false, min..Int.MAX_VALUE)

        fun nonnegative() = min(0)

        fun positive() = min(1)

        fun radixUnlimited() = IntegerArgumentType(true, null)
    }
}
