package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class SetStatement(
    sourceSpan: SourceSpan,
    diagnostics: List<BclDiagnostic>,
    semanticTokens: List<BclSemanticToken>,
    val identifier: String,
    val parameters: List<String>,
    val block: Block
) : Statement(sourceSpan, diagnostics, semanticTokens)
