package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Difficulty : StringRepresentable {
    NORMAL,
    PEACEFUL,
    EASY,
    HARD,
    P,
    E,
    N,
    H;

    override fun getString(): String {
        return name.lowercase()
    }
}
