package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.TypeAnnotation
import io.github.takenoko4096.bcl.ast.expression.Expression
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class VarStatement(
    sourceSpan: SourceSpan,
    val isReadonly: Boolean,
    val typeAnnotation: TypeAnnotation?,
    val identifier: String,
    val initialValue: Expression?
) : Statement(sourceSpan)
