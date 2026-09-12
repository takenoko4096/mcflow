package io.github.takenoko4096.bcl.brigadier.value.json

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class SpannedJsonArray(
    val list: List<SpannedJsonValue<*>>,
    sourceSpan: SourceSpan
) : SpannedJsonValue<List<SpannedJsonValue<*>>>(list, sourceSpan)
