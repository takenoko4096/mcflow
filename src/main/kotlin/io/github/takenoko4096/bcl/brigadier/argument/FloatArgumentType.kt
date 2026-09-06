package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class FloatArgumentType(private val range: ClosedRange<Float>?) : ArgumentType<Float>("float") {
    override fun parse(reader: TokenSeqReader): Argument<Float> {
        val start = reader.getCurrentTokenStartPosition()
        val f = reader.readFloat()
        val end = reader.getPreviousTokenEndPosition()

        if (range != null && f !in range) {
            throw reader.exception(start..end, "浮動小数点数の読み取りに失敗しました: この引数は $range の範囲内でなければなりませんが、 $f が渡されました")
        }

        return Argument(this, f, start..end)
    }

    companion object {
        fun float(range: ClosedRange<Float>? = null) = FloatArgumentType(range)
    }
}
