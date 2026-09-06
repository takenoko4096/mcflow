package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class IntegerArgumentType(
    private val range: ClosedRange<Int>?,
    private val signedDecimalOnly: Boolean
) : ArgumentType<Int>("integer") {
    override fun parse(reader: TokenSeqReader): Argument<Int> {
        val start = reader.getCurrentTokenStartPosition()
        val i = reader.readInteger(signedDecimalOnly)
        val end = reader.getPreviousTokenEndPosition()

        if (range != null && i !in range) {
            throw reader.exception(start..end, "整数の読み取りに失敗しました: この引数は $range の範囲内でなければなりませんが、 $i が渡されました")
        }

        return Argument(this, i, start..end)
    }

    companion object {
        fun integer(range: ClosedRange<Int>? = null) = IntegerArgumentType(range, true)

        /**
         * LONG とかは読めないよん
         */
        fun unlimited() = IntegerArgumentType(null, false)
    }
}
