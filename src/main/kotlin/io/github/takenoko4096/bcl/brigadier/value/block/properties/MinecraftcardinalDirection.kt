package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class MinecraftCardinalDirection : StringRepresentable {
    SOUTH,
    WEST,
    NORTH,
    EAST;

    override fun getString() = name.lowercase()
}