package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class FillMode : StringRepresentable {
    OUTLINE,
    HOLLOW,
    DESTROY,
    KEEP;

    override fun getString(): String {
        return name.lowercase()
    }
}
