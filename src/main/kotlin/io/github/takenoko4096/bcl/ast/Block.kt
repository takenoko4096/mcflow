package io.github.takenoko4096.bcl.ast

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class Block(
    sourceSpan: SourceSpan,
    val nodes: List<Node>
) : Node(sourceSpan, null)
