package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class HudVisibility : CommandEnumerable {
    HIDE,
    RESET;

    override fun getNames(): Set<String> {
        return setOf(name.lowercase())
    }
}
