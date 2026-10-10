package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import org.junit.jupiter.api.Test

object Test {
    @Test
    fun test() {
        val tokenizer = BclTokenizer("1abcdefghijklmn", interpretComments = false)
        println(tokenizer.tokenize().map { it.sourceSpan.getStringFromSourceCode(tokenizer.sourceCode) + " (${it.type})" })
    }
}
