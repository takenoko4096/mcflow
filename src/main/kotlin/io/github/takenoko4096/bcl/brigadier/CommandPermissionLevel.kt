package io.github.takenoko4096.bcl.brigadier

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class CommandPermissionLevel(val integer: Int) : StringRepresentable, CommandEnumerable {
    MEMBER(0),
    GAME_DIRECTOR(1),
    ADMINISTRATOR(2),
    HOST(3),
    OWNER(4);

    override fun getString(): String {
        return name.lowercase()
    }

    override fun getNames(): Set<String> {
        return setOf(getString())
    }
}
