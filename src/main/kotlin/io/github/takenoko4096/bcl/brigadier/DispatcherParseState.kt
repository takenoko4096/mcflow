package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.reader.TokenSeqReader

data class DispatcherParseState(
    val reader: TokenSeqReader,
    val arguments: Map<String, Argument<*>>,
    val route: List<CommandNode>,
    val literalCount: Int
)
