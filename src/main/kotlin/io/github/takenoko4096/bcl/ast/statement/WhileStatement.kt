package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.ast.Label
import io.github.takenoko4096.bcl.ast.Calculatable
import io.github.takenoko4096.bcl.ast.expression.Expression
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class WhileStatement(
    sourceSpan: SourceSpan,
    val prioritizeBlockEvaluation: Boolean,
    val label: Label?,
    val condition: Calculatable,
    val block: Block
) : Statement(sourceSpan)
