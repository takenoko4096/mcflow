package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class BooleanArgumentType : ArgumentType<Boolean>("boolean", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader): Argument<Boolean> {
        val start = reader.getCurrentTokenStartPosition()
        return Argument(this, reader.readBool(), start..reader.getPreviousTokenEndPosition())
    }
}
