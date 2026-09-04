package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.ast.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class IntegerArgumentType(
    private val range: ClosedRange<Int>?,
    private val signedDecimalOnly: Boolean
) : ArgumentType<Int>("integer") {
    override fun parse(reader: TokenSeqReader): Argument<Int> {
        val s = reader.getCurrentTokenStartPosition()
        val i = reader.readInteger(signedDecimalOnly)
        return Argument(this, i, s..reader.getPreviousTokenEndPosition())
    }

    companion object {
        fun integer(range: ClosedRange<Int>? = null) = IntegerArgumentType(range, true)

        /**
         * LONG とかは読めないよん
         */
        fun unlimited() = IntegerArgumentType(null, false)
    }
}
