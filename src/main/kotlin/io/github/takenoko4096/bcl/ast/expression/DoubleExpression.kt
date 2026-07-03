package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class DoubleExpression(sourceSpan: SourceSpan, val value: Double) : NumberExpression(sourceSpan)
