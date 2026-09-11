package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class PlayerWaypointsValues : StringRepresentable, CommandEnumerable {
    EVERYONE,
    OFF;

    override fun getString(): String {
        return name.lowercase()
    }

    override fun getNames(): Set<String> {
        return setOf(getString())
    }
}
