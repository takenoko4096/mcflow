package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Orientation : StringRepresentable {
    DOWN_EAST,
    DOWN_NORTH,
    DOWN_SOUTH,
    DOWN_WEST,
    UP_EAST,
    UP_NORTH,
    UP_SOUTH,
    UP_WEST,
    WEST_UP,
    EAST_UP,
    NORTH_UP,
    SOUTH_UP;

    override fun getString() = name.lowercase()
}