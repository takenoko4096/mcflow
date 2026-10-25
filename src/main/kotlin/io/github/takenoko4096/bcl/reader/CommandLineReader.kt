package io.github.takenoko4096.bcl.reader

import io.github.takenoko4096.bcl.tokenizer.SourcePosition
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
}
