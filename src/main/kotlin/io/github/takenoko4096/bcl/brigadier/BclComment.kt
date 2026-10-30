package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.parser.BclParseException
import io.github.takenoko4096.bcl.reader.CommandLineReader
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token

abstract class BclComment(val sourceSpan: SourceSpan) {
    class Line(sourceSpan: SourceSpan) : BclComment(sourceSpan)
    class Multiline(sourceSpan: SourceSpan) : BclComment(sourceSpan)
    class CommentCommandLine(sourceSpan: SourceSpan, val reader: CommandLineReader) : BclComment(sourceSpan)

    companion object {
        fun newCommentCommandLine(sourceCode: String, sourceSpan: SourceSpan): CommentCommandLine? {
            val string = sourceSpan.getStringFromSourceCode(sourceCode)
            if (!string.startsWith(KeyChar.COMMENT_COMMAND_PREFIX_SEQUENCE) || string.length <= 2) return null
            val command = string.substring(2)

            val offset = sourceSpan.start

            val tokenizer = BclTokenizer(command, extension = false, comments = null)
            val tokens = tokenizer.tokenize().map {
                val (start, end) = it.sourceSpan
                val newSourceSpan = (start + offset)..(end + offset)
                Token(it.id, it.type, it.value, newSourceSpan)
            }

            if (tokens.isEmpty()) {
                return null
            }

            val reader = CommandLineReader(
                sourceCode = sourceCode,
                tokens = tokens,
                exceptionConstructor = ::BclParseException,
                lastSourcePosition = tokens.last().sourceSpan.end
            )

            return CommentCommandLine(sourceSpan, reader)
        }
    }
}
