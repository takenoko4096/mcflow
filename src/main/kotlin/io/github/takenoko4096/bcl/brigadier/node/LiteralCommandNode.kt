package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.CommandContext

open class LiteralCommandNode(
    name: String,
    executor: (CommandContext.() -> Any?)?,
    children: Map<String, CommandNode>,
    redirects: List<CommandNode>
) : CommandNode(name, executor, children, redirects) {
    override fun toString(): String {
        return name
    }
}
