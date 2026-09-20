package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.TokenType

class FilePathArgumentType : ArgumentType<String>("filepath") {
    override fun parse(reader: TokenSeqReader): Argument<String> {
        val sb = StringBuilder()

        val start = reader.getCurrentTokenStartPosition()

        val t = reader.peek()
        if (t.type == TokenType.DOUBLE_QUOTED) {
            reader.next()
            sb.append(t.value)
        }
        else do {
            sb.append(reader.readCommandString(allowSingleSignOnly = true))
        }
        while (
            reader.testThenAdvance { (it `is` Symbol.DIVIDE)
                .apply {
                    if (this) sb.append(Symbol.DIVIDE.value)
                }
        })

        val end = reader.getPreviousTokenEndPosition()

        return Argument(this, sb.toString(), start..end)
    }
}
