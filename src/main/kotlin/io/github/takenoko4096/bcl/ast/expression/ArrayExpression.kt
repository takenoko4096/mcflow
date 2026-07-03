package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class ArrayExpression(
    sourceSpan: SourceSpan,
    val elements: List<Expression>
) : Expression(sourceSpan)
