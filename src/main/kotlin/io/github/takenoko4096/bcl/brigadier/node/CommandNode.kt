package io.github.takenoko4096.bcl.brigadier.node

sealed class CommandNode(
    val name: String,
    val executable: Boolean,
    val children: Map<String, CommandNode>
) {
    abstract override fun toString(): String
}
