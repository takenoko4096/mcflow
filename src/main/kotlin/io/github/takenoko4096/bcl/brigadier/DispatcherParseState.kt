package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.reader.TokenSeqReader

data class DispatcherParseState(
    val reader: TokenSeqReader,
    val arguments: List<Pair<ArgumentCommandNode<*>, Argument<*>>>,
    val route: List<CommandNode>,
    val literalCount: Int
)
