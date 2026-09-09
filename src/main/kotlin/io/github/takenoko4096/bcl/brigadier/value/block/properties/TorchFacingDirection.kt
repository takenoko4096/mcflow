package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class TorchFacingDirection : StringRepresentable {
    UNKNOWN,
    WEST,
    EAST,
    NORTH,
    SOUTH,
    TOP;

    override fun getString() = name.lowercase()
}