package io.github.takenoko4096.bcl.brigadier.node

class LiteralCommandNode(
    name: String,
    executable: Boolean,
    children: Map<String, CommandNode>
) : CommandNode(name, executable, children) {
    override fun toString(): String {
        return name
    }
}
