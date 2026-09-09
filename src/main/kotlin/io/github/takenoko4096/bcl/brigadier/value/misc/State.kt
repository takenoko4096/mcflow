package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class State : StringRepresentable {
    ENABLED,
    DISABLED;

    override fun getString(): String {
        return name.lowercase()
    }
}
