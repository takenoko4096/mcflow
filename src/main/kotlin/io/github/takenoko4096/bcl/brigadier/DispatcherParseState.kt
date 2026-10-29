package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode
import io.github.takenoko4096.bcl.reader.CommandLineReader
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

data class DispatcherParseState(
    val reader: CommandLineReader,
    val literals: List<Pair<LiteralCommandNode, SourceSpan>>,
    val arguments: List<Pair<ArgumentCommandNode<*>, ArgumentParseResult<*>>>,
    val route: List<CommandNode>,
    val literalCount: Int
)
