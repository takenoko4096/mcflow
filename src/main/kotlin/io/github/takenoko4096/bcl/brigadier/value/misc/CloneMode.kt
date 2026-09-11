package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class CloneMode : CommandEnumerable {
    NORMAL,
    FORCE,
    MOVE;

    override fun getNames(): Set<String> {
        return setOf(name.lowercase())
    }
}
