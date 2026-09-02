package io.github.takenoko4096.bcl.ast.argument

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.ast.Type
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class Argument(
    val type: Type,
    sourceSpan: SourceSpan
) : Node(sourceSpan)
