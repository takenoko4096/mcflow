package io.github.takenoko4096.bcl.completer

import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

data class CompletionCandidate(
    val node: CommandNode,
    val inputSourceSpan: SourceSpan?
)
