package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class MinecraftBlockFace : StringRepresentable {
    DOWN,
    UP,
    NORTH,
    SOUTH,
    WEST,
    EAST;

    override fun getString() = name.lowercase()
}