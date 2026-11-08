package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Label
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class BreakStatement(
    sourceSpan: SourceSpan,
    diagnostics: List<BclDiagnostic>,
    semanticTokens: List<BclSemanticToken>,
    val label: Label?
) : Statement(sourceSpan, diagnostics, semanticTokens)
