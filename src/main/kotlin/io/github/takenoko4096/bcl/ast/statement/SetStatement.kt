package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.ast.Parameter
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class SetStatement(
    sourceSpan: SourceSpan,
    identifier: String,
    parameters: List<Parameter>,
    block: Block
) : Statement(sourceSpan)
