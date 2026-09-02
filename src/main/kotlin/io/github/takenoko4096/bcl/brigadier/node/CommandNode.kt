package io.github.takenoko4096.bcl.brigadier.node

sealed class CommandNode(
    val children: Set<CommandNode>
)
