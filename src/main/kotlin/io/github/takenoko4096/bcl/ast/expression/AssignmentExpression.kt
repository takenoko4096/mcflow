package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.ast.AssignmentOperation
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class AssignmentExpression(
    sourceSpan: SourceSpan,
    val operation: AssignmentOperation,
    val target: Expression,
    val value: Expression
) : Expression(sourceSpan)
