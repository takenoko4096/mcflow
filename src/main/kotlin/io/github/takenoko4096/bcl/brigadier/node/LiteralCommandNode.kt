package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.CommandContext
import io.github.takenoko4096.bcl.brigadier.CommandPermissionCheckContext
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import org.eclipse.lsp4j.CompletionItem

open class LiteralCommandNode(
    name: String,
    executor: (CommandContext.() -> Any?)?,
    children: Map<String, CommandNode>,
    redirects: List<CommandNode>,
    predicate: (CommandPermissionCheckContext.() -> CommandPermissionCheckContext.Result)?,
    completerOverride: ((BclCompletionContext) -> List<CompletionItem>)? = null
) : CommandNode(name, executor, children, redirects, predicate, completerOverride) {
    override fun toString(): String {
        return name
    }
}
