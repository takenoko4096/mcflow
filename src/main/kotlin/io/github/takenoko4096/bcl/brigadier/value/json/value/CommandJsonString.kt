package io.github.takenoko4096.bcl.brigadier.value.json.value

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class CommandJsonString(
    val string: String,
    sourceSpan: SourceSpan
) : CommandJsonValue<String>(string, sourceSpan)
