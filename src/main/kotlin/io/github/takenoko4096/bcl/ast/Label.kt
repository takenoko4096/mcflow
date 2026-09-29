package io.github.takenoko4096.bcl.ast

import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Label(
    sourceSpan: SourceSpan,
    val name: String
) : Node(sourceSpan)
