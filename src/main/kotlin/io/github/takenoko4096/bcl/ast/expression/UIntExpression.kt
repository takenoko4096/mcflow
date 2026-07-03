package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class UIntExpression(sourceSpan: SourceSpan, val value: UInt) : NumberExpression(sourceSpan)
