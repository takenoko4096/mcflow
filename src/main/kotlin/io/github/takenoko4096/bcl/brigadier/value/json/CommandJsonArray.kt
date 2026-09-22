package io.github.takenoko4096.bcl.brigadier.value.json

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class CommandJsonArray(
    val list: List<CommandJsonValue<*>>,
    sourceSpan: SourceSpan
) : CommandJsonValue<List<CommandJsonValue<*>>>(list, sourceSpan)
