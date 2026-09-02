package io.github.takenoko4096.bcl.ast.expression

import io.github.takenoko4096.bcl.ast.Calculatable
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class MacroCallExpression(
    sourceSpan: SourceSpan,
    val identifier: String,
    val arguments: List<Calculatable>
) : Expression(sourceSpan)
