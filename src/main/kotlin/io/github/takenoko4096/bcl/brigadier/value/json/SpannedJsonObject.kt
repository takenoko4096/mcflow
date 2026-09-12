package io.github.takenoko4096.bcl.brigadier.value.json

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class SpannedJsonObject(
    val map: Map<String, SpannedJsonValue<*>>,
    val keySpans: Map<String, SourceSpan>,
    sourceSpan: SourceSpan
) : SpannedJsonValue<Map<String, SpannedJsonValue<*>>>(map, sourceSpan)
