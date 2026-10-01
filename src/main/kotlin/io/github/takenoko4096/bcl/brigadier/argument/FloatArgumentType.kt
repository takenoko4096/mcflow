package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.OpenableFloatRange
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class FloatArgumentType(
    private val range: OpenableFloatRange?
) : ArgumentType<Float>("float", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<Float> {
        val start = reader.getCurrentTokenStartPosition()
        val f = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (range != null && f !in range) {
            diagnostics.add(reader.diagnostic(start..end, "浮動小数点数の読み取りに失敗しました: この引数は $range の範囲内でなければなりませんが、 $f が渡されました"))
            return NoValuedArgument(this, start..end, diagnostics)
        }

        return Argument(this, f, start..end)
    }

    companion object {
        fun float() = FloatArgumentType(null)

        fun float(range: ClosedFloatingPointRange<Float>) = FloatArgumentType(OpenableFloatRange.from(range))

        fun positive() = FloatArgumentType(OpenableFloatRange.openRange(0f, Float.POSITIVE_INFINITY))

        fun nonnegative() = FloatArgumentType(OpenableFloatRange.openEndRange(0f, Float.POSITIVE_INFINITY))
    }
}
