package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.ast.BinaryOperation
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class BinaryExpression(
    sourceSpan: SourceSpan,
    val operation: BinaryOperation,
    val left: Calculatable,
    val right: Calculatable
) : Expression(sourceSpan)
