package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class LongExpression(sourceSpan: SourceSpan, val value: Long) : Expression(sourceSpan)
