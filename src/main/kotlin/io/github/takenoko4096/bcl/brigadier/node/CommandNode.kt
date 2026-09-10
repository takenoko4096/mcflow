package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.CommandContext

sealed class CommandNode(
    val name: String,
    val executor: (CommandContext.() -> Any?)?,
    val children: Map<String, CommandNode>
) {
    abstract override fun toString(): String

    protected fun getTreeUsage(linebreak: Boolean, depth: Int, self: String? = null): String {
        val indent = "    ".repeat(depth)

        val usage = if (linebreak) indent + (self ?: toString()) else self ?: (' ' + toString())

        return if (children.isEmpty()) {
            usage
        }
        else if (children.size == 1) {
            usage + children.values.first().getTreeUsage(false, depth)
        }
        else {
            val continuingMarker = (if (executor == null) (' ' + "...") else "")
            usage + continuingMarker + children.values.joinToString("") {
                '\n' + it.getTreeUsage(true, depth + 1)
            }
        }
    }
}
