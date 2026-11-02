package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.TokenType

class FilePathArgumentType(private val strict: Boolean) : ArgumentType<String>("filepath") {
    override fun parse(context: ArgumentParseContext): Argument<String> {
        val reader = context.reader
        val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

        val sb = StringBuilder()

        val start = reader.getCurrentTokenStartPosition()

        val t = reader.peek()
        if (t.type == TokenType.DOUBLE_QUOTED) {
            reader.next()
            sb.append(t.value)
        }
        else do {
            sb.append(reader.readCommandString(allowSingleSignOnly = true, allowFloat = true))
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

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, sourceSpan))

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
