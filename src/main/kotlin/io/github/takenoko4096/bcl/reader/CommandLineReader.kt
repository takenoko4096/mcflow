package io.github.takenoko4096.bcl.reader

import io.github.takenoko4096.bcl.parser.BclParseException
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token

class CommandLineReader(
    sourceCode: String,
    tokens: List<Token>,
    exceptionConstructor: ExceptionConstructor,
    val lastSourcePosition: SourcePosition,
    val preprocessedSemanticTokens: List<BclSemanticToken>
) : TokenSeqReader(sourceCode, tokens, exceptionConstructor) {
    override fun clone(): CommandLineReader {
        val cpy = CommandLineReader(sourceCode, tokens, exceptionConstructor, lastSourcePosition, preprocessedSemanticTokens)
        cpy.cursor = cursor
        return cpy
    }

    companion object {
        fun normal(reader: TokenSeqReader, extension: Boolean): CommandLineReader {
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

            val t = reader.peek()
            if (extension && t `is` Symbol.DIVIDE) {
                preprocessedSemanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, t.sourceSpan))
                reader.next()
            }

            var lastWasEscapedNewLine = false

            while (!reader.isOver()) {
                current = reader.peek()
                val start = reader.getCurrentTokenStartPosition()
                reader.next()
                val end = reader.getPreviousTokenEndPosition()

                if (extension && current `is` Symbol.ESCAPE && !reader.isOver() && reader.peek() `is` Symbol.NEWLINE) {
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
                preprocessedSemanticTokens
            )
        }

        fun comment(sourceCode: String, sourceSpan: SourceSpan): CommandLineReader? {
            val string = sourceSpan.getStringFromSourceCode(sourceCode)
            if (!string.startsWith(KeyChar.COMMENT_COMMAND_PREFIX_SEQUENCE)) return null

            val offset = sourceSpan.start

            val tokenizer = BclTokenizer(string, extension = false, comments = null)
            val tokens = tokenizer.tokenize().map {
                val (start, end) = it.sourceSpan
                val newSourceSpan = (start + offset)..(end + offset)
                if (it is Token.Invalid) Token.Invalid(it.id, it.type, it.message, newSourceSpan)
                else Token(it.id, it.type, it.value, newSourceSpan)
            }

            if (tokens.isEmpty()) {
                return null
            }

            val preprocessedSemanticTokens = mutableListOf<BclSemanticToken>()

            val reader = CommandLineReader(
                sourceCode = sourceCode,
                tokens = tokens,
                exceptionConstructor = ::BclParseException,
                lastSourcePosition = tokens.last().sourceSpan.end,
                preprocessedSemanticTokens
            )

            val start = reader.getCurrentTokenStartPosition()
            reader.expect(Symbol.HASH)
            reader.expect(Symbol.DIVIDE)
            val end = reader.getPreviousTokenEndPosition()

            preprocessedSemanticTokens.add(BclSemanticToken(BclSemanticTokenType.COMMENT, start..end))

            return reader
        }
    }
}
