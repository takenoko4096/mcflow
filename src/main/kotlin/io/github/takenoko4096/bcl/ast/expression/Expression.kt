package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class Expression(
    sourceSpan: SourceSpan,
    diagnostics: List<BclDiagnostic>,
    val semanticTokens: List<BclSemanticToken>
) : Node(sourceSpan, diagnostics)
