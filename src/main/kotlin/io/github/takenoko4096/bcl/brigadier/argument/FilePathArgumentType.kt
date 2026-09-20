package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.KeyChar

class FilePathArgumentType : ArgumentType<String>("filepath") {
    override fun parse(reader: TokenSeqReader): Argument<String> {
        /*val sb = StringBuilder()

        val start = reader.getCurrentTokenStartPosition()

        var index = start.index
        while (reader.sourceCode[index] !in KeyChar.COMMAND_WORD_TERMINATORS) {
            index++
            if (index >= reader.sourceCode.length) break
        }*/
        TODO()
    }
}
