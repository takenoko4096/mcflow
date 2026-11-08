package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.argument.type.ArgumentType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class ArgumentParseResult<T>(
    val type: ArgumentType<T>,
    open val sourceSpan: SourceSpan?
)
