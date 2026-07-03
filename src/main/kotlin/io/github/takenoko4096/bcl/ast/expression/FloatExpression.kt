package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class FloatExpression(sourceSpan: SourceSpan, val value: Float) : NumberExpression(sourceSpan)
