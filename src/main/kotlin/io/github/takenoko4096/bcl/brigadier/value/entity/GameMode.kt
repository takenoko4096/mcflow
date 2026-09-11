package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class GameMode(private val string: String?, private val int: Int? = null) : StringRepresentable, CommandEnumerable {
    DEFAULT("d", null),
    SURVIVAL("s", 0),
    CREATIVE("c", 1),
    ADVENTURE("a", 2),
    SPECTATOR(null, null);

    override fun getString(): String {
        return name.lowercase()
    }

    override fun getNames(): Set<String> {
        return setOfNotNull(getString(), string, int?.toString())
    }
}
