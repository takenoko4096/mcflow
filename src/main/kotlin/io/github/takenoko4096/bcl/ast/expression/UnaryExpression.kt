package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.ast.UnaryOperation
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class UnaryExpression(
    sourceSpan: SourceSpan,
    val operation: UnaryOperation,
    val target: Expression
) : Expression(sourceSpan)
