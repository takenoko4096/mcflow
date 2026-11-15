package io.github.takenoko4096.bcl.brigadier.value.json.value

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class CommandJsonBoolean(
    val boolean: Boolean,
    sourceSpan: SourceSpan
) : CommandJsonValue<Boolean>(boolean, sourceSpan)
