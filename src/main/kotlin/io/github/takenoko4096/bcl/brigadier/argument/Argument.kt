package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.argument.type.ArgumentType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Argument<T>(
    type: ArgumentType<T>,
    val value: T,
    sourceSpan: SourceSpan
) : ArgumentParseResult<T>(type, sourceSpan) {
    override fun toString(): String {
        return value.toString()
    }
}