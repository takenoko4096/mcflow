package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

abstract class JsonObjectArgumentType<T>(name: String) : ArgumentType<T>(name) {
    override fun parse(reader: TokenSeqReader): Argument<T> {
        val obj = reader.readJsonObject()
        val value = interpret(reader, obj)
        return Argument(this, value, obj.sourceSpan)
    }

    protected abstract fun interpret(reader: TokenSeqReader, obj: CommandJsonObject): T
}
