package io.github.takenoko4096.bcl.ast

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Argument<T>(
    val type: ArgumentType<T>,
    val value: T,
    sourceSpan: SourceSpan
) : Node(sourceSpan)
