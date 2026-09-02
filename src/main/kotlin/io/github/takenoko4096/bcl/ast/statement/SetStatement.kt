package io.github.takenoko4096.bcl.ast.statement

import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class SetStatement(
    sourceSpan: SourceSpan,
    val identifier: String,
    val parameters: List<String>,
    val block: Block
) : Statement(sourceSpan)
