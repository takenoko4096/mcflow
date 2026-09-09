package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class GameMode : StringRepresentable {
    DEFAULT,
    CREATIVE,
    SPECTATOR,
    SURVIVAL,
    ADVENTURE,
    D,
    C,
    S,
    A;

    override fun getString(): String {
        return name.lowercase()
    }
}
