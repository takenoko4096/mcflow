package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

abstract class JsonObjectArgumentType<T>(name: String) : ArgumentType<T>(name) {
    override fun parse(reader: TokenSeqReader): Argument<T> {
        val start = reader.getCurrentTokenStartPosition()
        val obj = reader.readJsonObject()
        val end = reader.getPreviousTokenEndPosition()

        val value = interpret(reader, obj, start..end)

        return Argument(this, value, start..end)
    }

    protected abstract fun interpret(reader: TokenSeqReader, obj: Map<String, Any?>, sourceSpan: SourceSpan): T
}
