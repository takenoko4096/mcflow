package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.ast.BinaryOperation
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class BinaryExpression(
    sourceSpan: SourceSpan,
    val operation: BinaryOperation,
    val left: Expression,
    val right: Expression
) : Expression(sourceSpan, null)
