package io.github.takenoko4096.bcl.ast.argument

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.brigadier.node.ArgumentNode
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

sealed class Argument(
    val definition: ArgumentNode,
    sourceSpan: SourceSpan
) : Node(sourceSpan)
