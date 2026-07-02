package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class ULongExpression(sourceSpan: SourceSpan, val value: ULong) : Expression(sourceSpan)
