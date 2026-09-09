package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Dimension : StringRepresentable {
    OVERWORLD,
    NETHER,
    THE_END;

    override fun getString(): String {
        return name.lowercase()
    }
}
