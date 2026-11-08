package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.ast.UnaryOperation
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class UnaryExpression(
    sourceSpan: SourceSpan,
    diagnostics: List<BclDiagnostic>,
    semanticTokens: List<BclSemanticToken>,
    val operation: UnaryOperation,
    val target: Expression
) : Expression(sourceSpan, diagnostics, semanticTokens)
