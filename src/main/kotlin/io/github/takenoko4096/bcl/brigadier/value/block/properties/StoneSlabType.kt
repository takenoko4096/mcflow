package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class StoneSlabType : StringRepresentable {
    SMOOTH_STONE,
    SANDSTONE,
    WOOD,
    COBBLESTONE,
    BRICK,
    STONE_BRICK,
    QUARTZ,
    NETHER_BRICK;

    override fun getString() = name.lowercase()
}