package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.expression.Expression
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class ReturnStatement(
    sourceSpan: SourceSpan,
    semanticTokens: List<BclSemanticToken>,
    val value: Expression?
) : Statement(sourceSpan, semanticTokens)
