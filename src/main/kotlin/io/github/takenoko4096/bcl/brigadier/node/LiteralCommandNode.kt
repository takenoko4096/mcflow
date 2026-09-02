package io.github.takenoko4096.bcl.brigadier.node

class LiteralCommandNode(
    val literal: String,
    children: Set<CommandNode>,
) : CommandNode(children)
