package io.github.takenoko4096.bcl.brigadier.node

import io.github.takenoko4096.bcl.brigadier.Command

class RootCommandNode(
    name: String,
    val commands: MutableMap<String, Command>
) : CommandNode(name, null, commands, listOf()) {
    override fun toString(): String {
        return name
    }
}
