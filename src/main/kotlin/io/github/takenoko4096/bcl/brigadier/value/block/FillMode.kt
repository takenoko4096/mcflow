package io.github.takenoko4096.bcl.brigadier.value.block

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class FillMode : CommandEnumerable {
    OUTLINE,
    HOLLOW,
    DESTROY,
    KEEP;

    override fun getNames(): Set<String> {
        return setOf(name.lowercase())
    }
}
