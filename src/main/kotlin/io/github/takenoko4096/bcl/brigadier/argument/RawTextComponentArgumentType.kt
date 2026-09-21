package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.value.json.SpannedJsonObject
import io.github.takenoko4096.bcl.brigadier.value.text.RawText
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class RawTextComponentArgumentType : JsonObjectArgumentType<RawText>("rawtext") {
    override fun interpret(reader: TokenSeqReader, obj: SpannedJsonObject, sourceSpan: SourceSpan): RawText {
        TODO("Not yet implemented")
    }
}
