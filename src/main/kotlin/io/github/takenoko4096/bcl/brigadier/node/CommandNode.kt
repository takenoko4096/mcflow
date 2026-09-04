package io.github.takenoko4096.bcl.brigadier.node

sealed class CommandNode(
    val executable: Boolean,
    val children: Map<String, CommandNode>
)
