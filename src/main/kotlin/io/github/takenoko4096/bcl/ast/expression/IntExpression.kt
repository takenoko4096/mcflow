package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class IntExpression(sourceSpan: SourceSpan, val value: Int) : NumberExpression(sourceSpan)
