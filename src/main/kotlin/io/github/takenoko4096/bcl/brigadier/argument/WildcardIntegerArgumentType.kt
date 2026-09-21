package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.Symbol

class WildcardIntegerArgumentType : ArgumentType<Int?>("wildcard_integer") {
    override fun parse(reader: TokenSeqReader): Argument<Int?> {
        val start = reader.getCurrentTokenStartPosition()

        val i = if (reader.peek() `is` Symbol.MULTIPLY) {
            reader.next()
            null
        }
        else {
            reader.readInteger(signedDecimalOnly = true)
        }

        val end = reader.getPreviousTokenEndPosition()

        return Argument(this, i, start..end)
    }
}
