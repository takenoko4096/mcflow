package io.github.takenoko4096.bcl.brigadier.value.location

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Axis : StringRepresentable {
    X,
    Y,
    Z;

    override fun getString(): String {
        return name.lowercase()
    }
}
