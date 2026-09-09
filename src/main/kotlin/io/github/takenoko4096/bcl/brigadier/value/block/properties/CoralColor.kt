package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class CoralColor : StringRepresentable {
    BLUE,
    PINK,
    PURPLE,
    RED,
    YELLOW;

    override fun getString() = name.lowercase()
}