package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.ast.Calculatable
import io.github.takenoko4096.bcl.ast.expression.Expression
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class IfElseStatement(
    sourceSpan: SourceSpan,
    val condition: Calculatable,
    val thenBlock: Block,
    val elseBlock: Block?
) : Statement(sourceSpan)
