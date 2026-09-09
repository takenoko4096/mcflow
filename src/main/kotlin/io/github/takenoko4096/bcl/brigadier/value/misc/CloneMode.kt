package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class CloneMode : StringRepresentable {
    NORMAL,
    FORCE,
    MOVE;

    override fun getString(): String {
        return name.lowercase()
    }
}
