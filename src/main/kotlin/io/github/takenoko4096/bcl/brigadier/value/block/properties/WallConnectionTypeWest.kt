package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class WallConnectionTypeWest : StringRepresentable {
    NONE,
    SHORT,
    TALL;

    override fun getString() = name.lowercase()
}