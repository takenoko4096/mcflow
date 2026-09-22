package io.github.takenoko4096.bcl.brigadier.value.json

import io.github.takenoko4096.bcl.SpanHolder
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

open class CommandJsonValue<T>(val value: T, override val sourceSpan: SourceSpan) : SpanHolder
