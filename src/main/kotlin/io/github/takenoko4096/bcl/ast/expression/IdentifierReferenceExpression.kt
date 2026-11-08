package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class IdentifierReferenceExpression(
    sourceSpan: SourceSpan,
    diagnostics: List<BclDiagnostic>,
    semanticTokens: List<BclSemanticToken>,
    val identifier: String
) : Expression(sourceSpan, diagnostics, semanticTokens)
