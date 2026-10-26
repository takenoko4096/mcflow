package io.github.takenoko4096.bcl.reader

import io.github.takenoko4096.bcl.parser.BclParseException
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token

class CommandLineReader(
    sourceCode: String,
    tokens: List<Token>,
    exceptionConstructor: ExceptionConstructor,
    val lastSourcePosition: SourcePosition
) : TokenSeqReader(sourceCode, tokens, exceptionConstructor) {
    override fun clone(): CommandLineReader {
        val cpy = CommandLineReader(sourceCode, tokens, exceptionConstructor, lastSourcePosition)
        cpy.cursor = cursor
        return cpy
    }

    companion object {
        fun from(reader: TokenSeqReader, extension: Boolean, semanticTokens: MutableList<BclSemanticToken>): CommandLineReader {
            val tokens = mutableListOf<Token>()
            var current: Token

            reader.skipNewLine()

            var lastSourcePosition: SourcePosition = if (reader.isOver()) {
                reader.getOverNextSourceSpan().start
            }
            else {
                reader.getCurrentTokenStartPosition()
            }

            while (!reader.isOver()) {
                current = reader.peek()
                val start = reader.getCurrentTokenStartPosition()
                reader.next()
                val end = reader.getPreviousTokenEndPosition()

                if (extension && current `is` Symbol.ESCAPE && !reader.isOver() && reader.peek() `is` Symbol.NEWLINE) {
                    reader.next()
                    lastSourcePosition = (if (reader.isOver()) reader.getOverNextSourceSpan().start else reader.peek().sourceSpan.start)
                    semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, start..end))
                    continue
                }

                if (current `is` Symbol.NEWLINE) {
                    break
                }

                tokens.add(current)
            }

            if (lastSourcePosition.index > tokens.last().sourceSpan.end.index) {
                lastSourcePosition = tokens.last().sourceSpan.end
            }

            return CommandLineReader(
                reader.sourceCode,
                tokens,
                ::BclParseException,
                lastSourcePosition
            )
        }
    }
}
