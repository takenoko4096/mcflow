package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class CharExpression(sourceSpan: SourceSpan, val value: Char) : Expression(sourceSpan)
