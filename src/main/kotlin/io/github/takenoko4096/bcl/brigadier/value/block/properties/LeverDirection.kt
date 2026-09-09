package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class LeverDirection : StringRepresentable {
    DOWN_EAST_WEST,
    EAST,
    WEST,
    SOUTH,
    NORTH,
    UP_NORTH_SOUTH,
    UP_EAST_WEST,
    DOWN_NORTH_SOUTH;

    override fun getString() = name.lowercase()
}