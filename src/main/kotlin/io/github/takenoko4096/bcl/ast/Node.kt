package io.github.takenoko4096.bcl.ast

import io.github.takenoko4096.bcl.SpanHolder
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

abstract class Node(override val sourceSpan: SourceSpan, val semanticTokenType: BclSemanticTokenType?) : SpanHolder
