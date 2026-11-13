package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.ast.AssignmentOperation
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class AssignmentExpression(
    sourceSpan: SourceSpan,
    diagnostics: List<BclDiagnostic>,
    semanticTokens: List<BclSemanticToken>,
    val operation: AssignmentOperation,
    val target: Expression,
    val value: Expression
) : Expression(sourceSpan, diagnostics, semanticTokens)
