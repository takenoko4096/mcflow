package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Calculatable
import io.github.takenoko4096.bcl.ast.expression.Expression
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class ReturnStatement(
    sourceSpan: SourceSpan,
    val value: Calculatable?
) : Statement(sourceSpan)
