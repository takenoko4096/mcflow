package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

data class BclSemanticToken(
    val type: BclSemanticTokenType,
    override val sourceSpan: SourceSpan
) : SpanHolder
