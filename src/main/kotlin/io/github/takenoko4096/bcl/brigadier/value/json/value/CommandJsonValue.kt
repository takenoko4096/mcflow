package io.github.takenoko4096.bcl.brigadier.value.json.value

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class CommandJsonValue<T>(
    val value: T,
    val sourceSpan: SourceSpan
)
