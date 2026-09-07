package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.CommandContext

open class LiteralCommandNode(
    name: String,
    executor: (CommandContext.() -> Any?)?,
    children: Map<String, CommandNode>
) : CommandNode(name, executor, children) {
    override fun toString(): String {
        return name
    }
}
