package io.github.takenoko4096.bcl.reader

import io.github.takenoko4096.bcl.brigadier.CommandSender
import io.github.takenoko4096.bcl.parser.BclParseException
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.`is`

class CommandLineReader private constructor(
    sourceCode: String,
    tokens: List<Token>,
    override val sourceSpan: SourceSpan,
    overSourcePosition: SourcePosition,
    exceptionConstructor: ExceptionConstructor,
    val preprocessedSemanticTokens: List<BclSemanticToken>,
    val isCommentCommand: Boolean
) : TokenSeqReader(sourceCode, tokens, sourceSpan, overSourcePosition, exceptionConstructor) {
    override fun clone(): CommandLineReader {
        val cpy = CommandLineReader(
            sourceCode,
            tokens,
            sourceSpan,
            overSourcePosition,
            exceptionConstructor,
            preprocessedSemanticTokens,
            isCommentCommand
        )
        cpy.cursor = cursor
        return cpy
    }

    companion object {
        /*fun of(reader: TokenSeqReader, sender: CommandSender): CommandLineReader {
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
            if (sender.extension && t `is` Symbol.DIVIDE) {
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

                if (sender.extension && !isCommentCommand && current `is` Symbol.ESCAPE && !reader.isOver() && reader.peek() `is` Symbol.NEWLINE) {
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
                sourceSpan = null,
                overSourcePosition = null,
                lastSourcePosition,
                ::BclParseException,
                preprocessedSemanticTokens,
                isCommentCommand = isCommentCommand
            )
        }*/

        fun of(reader: TokenSeqReader, sender: CommandSender): CommandLineReader {
            val sourceCode = reader.sourceCode
            val preprocessedSemanticTokens = mutableListOf<BclSemanticToken>()
            val tokens = mutableListOf<Token>()

            reader.skipNewLine()

            val startPosition = if (reader.isOver()) {
                reader.overSourcePosition
            }
            else {
                reader.getCurrentTokenStartPosition()
            }

            // コマンド範囲の終端位置（exclusive）
            var endExclusivePosition = startPosition

            var isCommentCommand = false
            var terminatedByNewLine = false

            if (!reader.isOver()) {
                val t = reader.peek()

                if (sender.extension && t `is` Symbol.DIVIDE) {
                    preprocessedSemanticTokens.add(
                        BclSemanticToken(BclSemanticTokenType.SYMBOL, t.sourceSpan)
                    )
                    reader.next()
                    endExclusivePosition = SourcePosition.toSourcePosition(
                        sourceCode,
                        t.sourceSpan.end.index + 1
                    )!!
                }
                else if (reader.peek(0) `is` Symbol.HASH && reader.peek(1) `is` Symbol.DIVIDE) {
                    val hash = reader.peek(0)!!
                    val divide = reader.peek(1)!!

                    preprocessedSemanticTokens.add(
                        BclSemanticToken(BclSemanticTokenType.COMMENT, hash.sourceSpan)
                    )
                    reader.next()

                    preprocessedSemanticTokens.add(
                        BclSemanticToken(BclSemanticTokenType.COMMENT, divide.sourceSpan)
                    )
                    reader.next()

                    endExclusivePosition = SourcePosition.toSourcePosition(
                        sourceCode,
                        divide.sourceSpan.end.index + 1
                    )!!

                    isCommentCommand = true
                }
            }

            while (!reader.isOver()) {
                val current = reader.peek()
                reader.next()

                // 通常の改行はコマンドの終端。
                if (current `is` Symbol.NEWLINE) {
                    endExclusivePosition = current.sourceSpan.start
                    terminatedByNewLine = true
                    break
                }

                // エスケープ改行はコマンドの一部として消費する。
                if (
                    sender.extension &&
                    !isCommentCommand &&
                    current `is` Symbol.ESCAPE &&
                    !reader.isOver() &&
                    reader.peek() `is` Symbol.NEWLINE
                ) {
                    val newline = reader.peek()
                    reader.next()

                    preprocessedSemanticTokens.add(
                        BclSemanticToken(
                            BclSemanticTokenType.SYMBOL,
                            current.sourceSpan
                        )
                    )

                    endExclusivePosition = SourcePosition.toSourcePosition(
                        sourceCode,
                        newline.sourceSpan.end.index + 1
                    )!!

                    continue
                }

                tokens.add(current)

                endExclusivePosition = SourcePosition.toSourcePosition(
                    sourceCode,
                    current.sourceSpan.end.index + 1
                )!!
            }

            // 通常の改行で終了していなければ、ReaderのEOFまでがコマンド範囲。
            // getOverNextSourceSpan() のように tokens.last() から推定してはいけない。
            if (!terminatedByNewLine) {
                endExclusivePosition = reader.overSourcePosition
            }

            // exclusiveな範囲をinclusiveなSourceSpanに変換する。
            val sourceSpan = run {
                check(startPosition.index < endExclusivePosition.index) {
                    "CommandLineReader must have a non-empty source range"
                }

                val endPosition = SourcePosition.toSourcePosition(
                    sourceCode,
                    endExclusivePosition.index - 1
                )!!

                startPosition..endPosition
            }

            return CommandLineReader(
                sourceCode = sourceCode,
                tokens = tokens,
                sourceSpan = sourceSpan,
                overSourcePosition = endExclusivePosition,
                exceptionConstructor = ::BclParseException,
                preprocessedSemanticTokens = preprocessedSemanticTokens,
                isCommentCommand = isCommentCommand
            )
        }
    }
}
