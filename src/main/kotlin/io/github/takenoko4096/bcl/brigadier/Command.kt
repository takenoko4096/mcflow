package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode

class Command(
    name: String,
    override val description: String,
    val aliases: Set<String>,
    executor: (CommandContext.() -> Any?)?,
    children: Map<String, CommandNode>
) : LiteralCommandNode(name, description, executor, children) {
    fun getAllUsages(): List<String> {
        val usage = "/$name" + if (aliases.isEmpty()) "" else aliases.joinToString(", ", "[", "]")

        return getChildrenUsagesOf(this, usage)
    }

    private fun getChildrenUsagesOf(node: CommandNode, parent: String): List<String> {
        val usages = mutableListOf<String>()

        for ((_, value) in node.children) {
            val usage = "$parent $value"

            if (value.executor != null) {
                if (value.description == null) {
                    usages.add(usage)
                }
                else {
                    usages.add("$usage\n    - ${value.description!!}")
                }
            }

            usages.addAll(getChildrenUsagesOf(value, usage))
        }

        return usages
    }

    override fun toString(): String {
        return if (aliases.isEmpty()) {
            "Command</$name>"
        }
        else {
            "Command</$name $aliases>"
        }
    }
}
