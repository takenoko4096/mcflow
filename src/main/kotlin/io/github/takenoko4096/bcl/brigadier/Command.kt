package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.CommandNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode

class Command(
    name: String,
    val description: String,
    val aliases: Set<String>,
    executor: (CommandContext.() -> Any?)?,
    children: Map<String, CommandNode>,
    redirects: List<CommandNode> = listOf(),
    predicate: (CommandPermissionCheckContext.() -> CommandPermissionCheckContext.Result)?
) : LiteralCommandNode(name, executor, children, redirects, predicate) {
    fun getAllUsages(): List<String> {
        return getAllUsages("/$name" + if (aliases.isEmpty()) "" else aliases.joinToString(", ", "[", "]"))
    }

    fun getTreeUsage(): String {
        return getTreeUsage(
            false,
            0,
            self = "/$name" + if (aliases.isEmpty()) "" else aliases.joinToString(", ", "[", "]")
        )
    }

    override fun toString(): String {
        return if (aliases.isEmpty()) {
            "/$name"
        }
        else {
            "/$name$aliases"
        }
    }

    companion object {
        fun builder(name: String, configurator: CommandNodeBuilder.CommandBuilder.() -> Unit): CommandNodeBuilder.CommandBuilder {
            return CommandNodeBuilder.CommandBuilder(name, configurator)
        }
    }
}
