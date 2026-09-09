package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class ScoreboardPlayersNumAction : StringRepresentable {
    SET,
    ADD,
    REMOVE;

    override fun getString(): String {
        return name.lowercase()
    }
}
