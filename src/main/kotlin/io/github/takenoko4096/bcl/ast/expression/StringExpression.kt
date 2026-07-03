package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class StringExpression(sourceSpan: SourceSpan, val value: String) : Expression(sourceSpan)
