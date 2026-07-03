package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class FunctionCallExpression(
    sourceSpan: SourceSpan,
    val identifier: String,
    val arguments: List<Expression>
) : Expression(sourceSpan)
