package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.CommandContext

open class LiteralCommandNode(
    name: String,
    description: String?,
    executor: (CommandContext.() -> Any?)?,
    children: Map<String, CommandNode>
) : CommandNode(name, description, executor, children) {
    override fun toString(): String {
        return name
    }
}
