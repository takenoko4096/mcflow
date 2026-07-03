package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class VariableReferenceExpression(sourceSpan: SourceSpan, val identifier: String) : Expression(sourceSpan)
