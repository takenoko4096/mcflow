package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType

class ArgumentCommandNode<T>(
    val type: ArgumentType<T>,
    executable: Boolean,
    children: Map<String, CommandNode>
) : CommandNode(executable, children)
