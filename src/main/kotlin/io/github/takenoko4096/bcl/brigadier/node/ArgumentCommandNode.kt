package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType

class ArgumentCommandNode<T>(
    val name: String,
    val type: ArgumentType<T>,
    children: Set<CommandNode>
) : CommandNode(children)
