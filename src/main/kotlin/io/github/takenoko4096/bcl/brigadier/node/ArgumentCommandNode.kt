package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.argument.ArgumentType

class ArgumentCommandNode<T>(
    name: String,
    val type: ArgumentType<T>,
    executable: Boolean,
    children: Map<String, CommandNode>
) : CommandNode(name, executable, children) {
    override fun toString(): String {
        return "<$name: ${type.name}>"
    }
}
