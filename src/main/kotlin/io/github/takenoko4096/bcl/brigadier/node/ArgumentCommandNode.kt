package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.CommandContext
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType

class ArgumentCommandNode<T>(
    name: String,
    val type: ArgumentType<T>,
    executor: (CommandContext.() -> Any?)?,
    children: Map<String, CommandNode>,
    redirects: List<CommandNode>
) : CommandNode(name, executor, children, redirects) {
    override fun toString(): String {
        return "<$name: ${type.name}>"
    }
}
