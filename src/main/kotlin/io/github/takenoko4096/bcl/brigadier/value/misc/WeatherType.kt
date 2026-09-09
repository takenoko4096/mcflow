package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class WeatherType : StringRepresentable {
    CLEAR,
    RAIN,
    THUNDER;

    override fun getString(): String {
        return name.lowercase()
    }
}
