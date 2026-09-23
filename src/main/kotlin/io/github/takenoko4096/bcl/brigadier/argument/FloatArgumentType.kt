package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class FloatArgumentType(private val validator: (TokenSeqReader.(Float, SourceSpan) -> Unit)?) : ArgumentType<Float>("float", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader): Argument<Float> {
        val start = reader.getCurrentTokenStartPosition()
        val f = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        validator?.invoke(reader, f, start..end)

        return Argument(this, f, start..end)
    }

    companion object {
        fun float() = FloatArgumentType(null)

        fun float(range: ClosedFloatingPointRange<Float>) = FloatArgumentType { value, sourceSpan ->
            if (value !in range) {
                throw exception(sourceSpan, "浮動小数点数の読み取りに失敗しました: この引数は $range の範囲内でなければなりませんが、 $value が渡されました")
            }
        }

        fun positive() = FloatArgumentType { value, sourceSpan ->
            if (value <= 0f) {
                throw exception(sourceSpan, "浮動小数点数の読み取りに失敗しました: この引数は正の値でなければなりませんが、 $value が渡されました")
            }
        }

        fun nonnegative() = FloatArgumentType { value, sourceSpan ->
            if (value < 0f) {
                throw exception(sourceSpan, "浮動小数点数の読み取りに失敗しました: この引数は非負の値でなければなりませんが、 $value が渡されました")
            }
        }
    }
}
