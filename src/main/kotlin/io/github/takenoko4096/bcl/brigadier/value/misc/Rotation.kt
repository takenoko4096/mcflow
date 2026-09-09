package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Rotation : StringRepresentable {
    0_DEGREES,
    90_DEGREES,
    180_DEGREES,
    270_DEGREES;

    override fun getString(): String {
        return name.lowercase()
    }
}
