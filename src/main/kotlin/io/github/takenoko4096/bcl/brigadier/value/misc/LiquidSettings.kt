package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class LiquidSettings : StringRepresentable {
    APPLY_WATERLOGGING,
    IGNORE_WATERLOGGING;

    override fun getString(): String {
        return name.lowercase()
    }
}
