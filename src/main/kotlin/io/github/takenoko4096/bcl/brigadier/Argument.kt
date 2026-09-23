package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Argument<T>(
    type: ArgumentType<T>,
    val value: T,
    sourceSpan: SourceSpan,
    semanticTokens: List<BclSemanticToken> = listOf() // TODO
) : ArgumentParseResult<T>(type, sourceSpan, semanticTokens) {
    override fun toString(): String {
        return value.toString()
    }
}
