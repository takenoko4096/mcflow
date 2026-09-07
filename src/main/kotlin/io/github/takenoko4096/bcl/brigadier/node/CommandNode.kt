package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.CommandContext

sealed class CommandNode(
    val name: String,
    val executor: (CommandContext.() -> Any?)?,
    val children: Map<String, CommandNode>
) {
    abstract override fun toString(): String
}
