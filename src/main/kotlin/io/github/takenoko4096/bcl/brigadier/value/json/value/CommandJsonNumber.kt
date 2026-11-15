package io.github.takenoko4096.bcl.brigadier.value.json.value

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class CommandJsonNumber(
    val number: Number,
    sourceSpan: SourceSpan
) : CommandJsonValue<Number>(number, sourceSpan)
