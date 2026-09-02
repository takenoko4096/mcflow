package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.ast.Label
import io.github.takenoko4096.bcl.ast.Calculatable
import io.github.takenoko4096.bcl.ast.expression.Expression
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class ForStatement(
    sourceSpan: SourceSpan,
    val label: Label?,
    val elementVariableIdentifier: String,
    val iterable: Calculatable,
    val block: Block
) : Statement(sourceSpan)
