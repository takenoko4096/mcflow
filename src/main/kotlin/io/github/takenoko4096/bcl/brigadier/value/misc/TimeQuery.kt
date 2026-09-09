package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class TimeQuery : StringRepresentable {
    DAYTIME,
    GAMETIME,
    DAY;

    override fun getString(): String {
        return name.lowercase()
    }
}
