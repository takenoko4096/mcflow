package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode

class Command(
    name: String,
    val description: String,
    val aliases: Set<String>,
    executable: Boolean,
    children: Map<String, CommandNode>
) : LiteralCommandNode(name, executable, children) {
    fun getAllUsages(): List<String> {
        val usage = "/$name" + aliases.joinToString(", ", "[", "]").ifEmpty { "" }

        return getChildrenUsagesOf(this, usage)
    }

    private fun getChildrenUsagesOf(node: CommandNode, parent: String): List<String> {
        val usages = mutableListOf<String>()

        for ((_, value) in node.children) {
            val usage = "$parent $value"

            if (value.executable) {
                usages.add(usage)
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
