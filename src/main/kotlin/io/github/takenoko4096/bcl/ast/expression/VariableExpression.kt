package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class VariableExpression(sourceSpan: SourceSpan, val identifier: String) : Expression(sourceSpan)
