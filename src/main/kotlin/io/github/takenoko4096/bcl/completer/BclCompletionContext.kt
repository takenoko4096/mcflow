package io.github.takenoko4096.bcl.completer

import io.github.takenoko4096.bcl.brigadier.CommandSender
import io.github.takenoko4096.bcl.brigadier.dispatcher.CommandDispatcher
import io.github.takenoko4096.bcl.brigadier.node.Command
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

data class BclCompletionContext(
    val string: String,
    val offset: SourcePosition,
    val sourceSpan: SourceSpan?,
    val sourceCode: String,
    val sender: CommandSender,
    val dispatcher: CommandDispatcher,
    val route: List<CommandNode>
) {
    fun newCompleter() = BclCompleter(this)
}
