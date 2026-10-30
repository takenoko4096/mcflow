package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.parser.BclParseException
import io.github.takenoko4096.bcl.reader.CommandLineReader
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.Symbol
import org.junit.jupiter.api.Test

object Test {
    @Test
    fun test() {
        val tokenizer = BclTokenizer("#/permission-level 10", extension = false, comments = null)
        val tokens = tokenizer.tokenize()
        println(tokens.map { it.sourceSpan.getStringFromSourceCode(tokenizer.sourceCode) + " (${it.type})" })
        val reader = CommandLineReader(tokenizer.sourceCode, tokens, ::BclParseException, tokens.last().sourceSpan.end)
        reader.expect(Symbol.HASH)
        reader.expect(Symbol.DIVIDE)
        println(reader.readCommandString())
    }
}
