package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.ast.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class IntegerArgumentType : ArgumentType<Int>("integer") {
    override fun parse(reader: TokenSeqReader): Argument<Int> {
        val s = reader.getCurrentTokenStartPosition()
        val i = reader.readInteger()
        return Argument(this, i, s..reader.getPreviousTokenEndPosition())
    }
}
