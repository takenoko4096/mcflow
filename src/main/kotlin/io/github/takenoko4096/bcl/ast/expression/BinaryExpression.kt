package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.ast.BinaryOperation
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class BinaryExpression(
    sourceSpan: SourceSpan,
    diagnostics: List<BclDiagnostic>,
    semanticTokens: List<BclSemanticToken>,
    val operation: BinaryOperation,
    val left: Expression,
    val right: Expression
) : Expression(sourceSpan, diagnostics, semanticTokens)
