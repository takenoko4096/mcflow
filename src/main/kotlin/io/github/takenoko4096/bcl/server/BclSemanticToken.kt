package io.github.takenoko4096.bcl.server

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

data class BclSemanticToken(val type: BclSemanticTokenType, val sourceSpan: SourceSpan)
