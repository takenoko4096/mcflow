package io.github.takenoko4096.bcl.brigadier.node

class LiteralCommandNode(
    executable: Boolean,
    children: Map<String, CommandNode>
) : CommandNode(executable, children) {

}
