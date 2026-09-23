package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.TokenType

class FilePathArgumentType(private val strict: Boolean) : ArgumentType<String>("filepath") {
    override fun parse(reader: TokenSeqReader): Argument<String> {
        val sb = StringBuilder()

        val start = reader.getCurrentTokenStartPosition()

        val t = reader.peek()
        if (t.type == TokenType.DOUBLE_QUOTED) {
            reader.next()
            sb.append(t.value)
        }
        else do {
            sb.append(reader.readCommandStringAsFatal(allowSingleSignOnly = true, allowFloat = true))
        }
        while (
            reader.testThenAdvance(
                predicate = { reader.peek() `is` Symbol.DIVIDE },
                preAdvance = { sb.append(reader.getWhitespaceBetweenPreviousAndCurrent()) },
                postAdvance = { sb.append(Symbol.DIVIDE.value).append(reader.getWhitespaceBetweenPreviousAndCurrent()) }
            )
        )

        val end = reader.getPreviousTokenEndPosition()

        val filepath = sb.toString()
        val sourceSpan = start..end

        if (strict) {
            for (char in KeyChar.COMMAND_WORD_TERMINATORS) {
                if (char == KeyChar.SLASH) continue

                if (char in filepath) {
                    throw reader.exception(sourceSpan, "ファイルパスの読み取りに失敗しました: 厳格モードのファイルパスには文字 '$char' を含めることができません")
                }
            }
        }

        return Argument(this, filepath, sourceSpan)
    }
}
