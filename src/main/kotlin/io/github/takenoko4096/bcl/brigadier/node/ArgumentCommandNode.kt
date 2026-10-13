package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.CommandContext
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import org.eclipse.lsp4j.CompletionItem

class ArgumentCommandNode<T>(
    name: String,
    val type: ArgumentType<T>,
    executor: (CommandContext.() -> Any?)?,
    children: Map<String, CommandNode>,
    redirects: List<CommandNode>,
    completerOverride: ((BclCompletionContext) -> List<CompletionItem>)? = null
) : CommandNode(name, executor, children, redirects, completerOverride) {
    override fun toString(): String {
        return "<$name: ${type.name}>"
    }
}
