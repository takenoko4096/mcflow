package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Argument<T>(
    val type: ArgumentType<T>,
    val value: T,
    sourceSpan: SourceSpan
) : Node(sourceSpan) {
    override fun toString(): String {
        return value.toString()
    }
}
