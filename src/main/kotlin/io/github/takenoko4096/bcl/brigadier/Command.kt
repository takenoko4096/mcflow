package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.node.LiteralCommandNode

class Command(
    val description: String,
    val aliases: Set<String>,
    val root: LiteralCommandNode
) {
    override fun toString(): String {
        return "Command(description=$description, aliases=$aliases) $root"
    }
}
