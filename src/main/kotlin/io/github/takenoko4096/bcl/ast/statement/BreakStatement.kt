package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Label
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class BreakStatement(
    sourceSpan: SourceSpan,
    val label: Label?
) : Statement(sourceSpan)
