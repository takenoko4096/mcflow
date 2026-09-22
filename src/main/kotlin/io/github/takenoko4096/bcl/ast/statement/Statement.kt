package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class Statement(sourceSpan: SourceSpan, semanticTokenType: BclSemanticTokenType?) : Node(sourceSpan, semanticTokenType)
