package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.ArgumentCommandNode
import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode

class Command(
    val name: String,
    val description: String,
    val aliases: Set<String>,
    val root: LiteralCommandNode
) {
    fun getAllUsages(): List<String> {
        val usage = "/$name" + aliases.joinToString(", ", "[", "]").ifEmpty { "" }

        return getChildrenUsagesOf(root, usage)
    }

    private fun getChildrenUsagesOf(node: CommandNode, parent: String): List<String> {
        val usages = mutableListOf<String>()

        for ((key, value) in node.children) {
            val usage = when (value) {
                is LiteralCommandNode -> "$parent $key"
                is ArgumentCommandNode<*> -> "$parent <$key: ${value.type.identifier}>"
            }

            if (value.executable) {
                usages.add(usage)
            }

            usages.addAll(getChildrenUsagesOf(value, usage))
        }

        return usages
    }

    override fun toString(): String {
        return "Command</$name ${aliases.ifEmpty { "" }}>"
    }
}
