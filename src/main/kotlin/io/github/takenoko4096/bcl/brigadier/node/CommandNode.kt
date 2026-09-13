package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.Command
import io.github.takenoko4096.bcl.brigadier.CommandContext

sealed class CommandNode(
    val name: String,
    val executor: (CommandContext.() -> Any?)?,
    val children: Map<String, CommandNode>,
    var redirects: List<CommandNode>
) {
    private fun getRouteFor(target: CommandNode, list: List<CommandNode>): List<CommandNode>? {
        for ((_, child) in children) {
            return when (child) {
                target -> list + child
                in list -> null
                else -> child.getRouteFor(target, list + child)
            } ?: continue
        }

        return null
    }

    fun getNamedParent(command: Command, name: String): CommandNode? {
        val list = (command as CommandNode).getRouteFor(this, listOf())!!

        var current: CommandNode = command
        for (node in list) {
            current = current.children[node.name]!!
            if (node.name == name) return current
        }

        return if (command.name == name) command else null
    }

    fun getChildrenAndRedirects(): Map<String, CommandNode> {
        return redirects.flatMap { it.getChildrenAndRedirects().entries }.associate { it.key to it.value } + children
    }

    abstract override fun toString(): String

    protected fun getAllUsages(string: String): List<String> {
        val usages = mutableListOf<String>()

        for (value in children.values) {
            val usage = "$string $value"

            if (value.executor != null) {
                usages.add(usage)
            }

            usages.addAll(value.getAllUsages(usage))
        }

        for (redirect in redirects) {
            val usage = "$string -> [${redirect.name}]"
            usages.add(usage)
        }

        return usages
    }

    protected fun getTreeUsage(linebreak: Boolean, depth: Int, self: String? = null): String {
        val indent = "    ".repeat(depth)

        val usage = if (linebreak) indent + (self ?: toString()) else self ?: (' ' + toString())

        return if (children.isEmpty() && redirects.isEmpty()) {
            usage
        }
        else if (children.size + redirects.size == 1) {
            if (redirects.isEmpty()) {
                usage + children.values.first().getTreeUsage(false, depth)
            }
            else {
                "$usage -> [${redirects.first().name}]"
            }
        }
        else {
            val willContinue = if (executor == null) " ..." else ""

            val childrenString = children.values.joinToString("") { '\n' + it.getTreeUsage(true, depth + 1) }
            val redirectsString = redirects.joinToString("") { "\n-> [${it.name}]" }

            usage + willContinue + childrenString + redirectsString
        }
    }
}
