package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class IntExpression(
    val value: Int,
    sourceSpan: SourceSpan
) : Expression(sourceSpan, BclSemanticTokenType.NUMBER)
