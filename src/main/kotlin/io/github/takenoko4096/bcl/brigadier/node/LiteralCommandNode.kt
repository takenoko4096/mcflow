package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.dispatcher.CommandExecuteContext
import io.github.takenoko4096.bcl.brigadier.dispatcher.CommandRequireContext
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import org.eclipse.lsp4j.CompletionItem

open class LiteralCommandNode(
    name: String,
    executor: (CommandExecuteContext.() -> Any?)?,
    children: Map<String, CommandNode>,
    redirects: List<CommandNode>,
    predicate: (CommandRequireContext.() -> CommandRequireContext.Result)?,
    completerOverride: ((BclCompletionContext) -> List<CompletionItem>)? = null
) : CommandNode(name, executor, children, redirects, predicate, completerOverride) {
    override fun toString(): String {
        return name
    }
}
