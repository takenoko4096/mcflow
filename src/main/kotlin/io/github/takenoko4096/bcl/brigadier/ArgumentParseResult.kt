package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.BclSemanticToken
import io.github.takenoko4096.bcl.SpanHolder
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class ArgumentParseResult<T>(
    val type: ArgumentType<T>,
    override val sourceSpan: SourceSpan,
    val semanticTokens: List<BclSemanticToken>
) : SpanHolder {
    class Failure<T>(
        type: ArgumentType<T>,
        sourceSpan: SourceSpan,
        val diagnostics: MutableList<BclDiagnostic>,
        semanticTokens: List<BclSemanticToken> = listOf() // TODO
    ) : ArgumentParseResult<T>(type, sourceSpan, semanticTokens)
}
