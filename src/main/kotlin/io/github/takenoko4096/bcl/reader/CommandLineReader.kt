package io.github.takenoko4096.bcl.reader

import io.github.takenoko4096.bcl.parser.BclParseException
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.`is`

class CommandLineReader private constructor(
    sourceCode: String,
    tokens: List<Token>,
    exceptionConstructor: ExceptionConstructor,
    val lastSourcePosition: SourcePosition,
    val preprocessedSemanticTokens: List<BclSemanticToken>,
    val isCommentCommand: Boolean
) : TokenSeqReader(sourceCode, tokens, exceptionConstructor) {
    override fun clone(): CommandLineReader {
        val cpy = CommandLineReader(sourceCode, tokens, exceptionConstructor, lastSourcePosition, preprocessedSemanticTokens, isCommentCommand)
        cpy.cursor = cursor
        return cpy
    }

    companion object {
        fun of(reader: TokenSeqReader, extension: Boolean): CommandLineReader {
            val preprocessedSemanticTokens = mutableListOf<BclSemanticToken>()
            val tokens = mutableListOf<Token>()
            var current: Token

            reader.skipNewLine()

            var lastSourcePosition: SourcePosition = if (reader.isOver()) {
                reader.getOverNextSourceSpan().start
            }
            else {
                reader.getCurrentTokenStartPosition()
            }

            var isCommentCommand = false

            val t = reader.peek()
            if (extension && t `is` Symbol.DIVIDE) {
                preprocessedSemanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, t.sourceSpan))
                reader.next()
            }
            else if (reader.peek(0) `is` Symbol.HASH && reader.peek(1) `is` Symbol.DIVIDE) {
                preprocessedSemanticTokens.add(BclSemanticToken(BclSemanticTokenType.COMMENT, reader.peek().sourceSpan))
                reader.next()
                preprocessedSemanticTokens.add(BclSemanticToken(BclSemanticTokenType.COMMENT, reader.peek().sourceSpan))
                reader.next()
                isCommentCommand = true
            }

            var lastWasEscapedNewLine = false

            while (!reader.isOver()) {
                current = reader.peek()
                val start = reader.getCurrentTokenStartPosition()
                reader.next()
                val end = reader.getPreviousTokenEndPosition()

                if (extension && !isCommentCommand && current `is` Symbol.ESCAPE && !reader.isOver() && reader.peek() `is` Symbol.NEWLINE) {
                    reader.next()
                    lastSourcePosition = reader.getOverNextSourceSpan().start
                    lastWasEscapedNewLine = true
                    preprocessedSemanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, start..end))
                    continue
                }

                if (current `is` Symbol.NEWLINE) {
                    break
                }

                tokens.add(current)

                lastSourcePosition = end
                lastWasEscapedNewLine = false
            }

            if (!lastWasEscapedNewLine && tokens.isNotEmpty()) {
                lastSourcePosition = tokens.last().sourceSpan.end
            }

            return CommandLineReader(
                reader.sourceCode,
                tokens,
                ::BclParseException,
                lastSourcePosition,
                preprocessedSemanticTokens,
                isCommentCommand = isCommentCommand
            )
        }
    }
}
