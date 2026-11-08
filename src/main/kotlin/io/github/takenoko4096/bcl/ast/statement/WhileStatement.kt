package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.ast.Label
import io.github.takenoko4096.bcl.ast.expression.Expression
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class WhileStatement(
    sourceSpan: SourceSpan,
    diagnostics: List<BclDiagnostic>,
    semanticTokens: List<BclSemanticToken>,
    val prioritizeBlockEvaluation: Boolean,
    val label: Label?,
    val condition: Expression,
    val block: Block
) : Statement(sourceSpan, diagnostics, semanticTokens)
