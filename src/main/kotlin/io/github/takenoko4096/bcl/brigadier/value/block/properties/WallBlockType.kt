package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class WallBlockType : StringRepresentable {
    COBBLESTONE,
    MOSSY_COBBLESTONE,
    GRANITE,
    DIORITE,
    ANDESITE,
    SANDSTONE,
    BRICK,
    STONE_BRICK,
    MOSSY_STONE_BRICK,
    NETHER_BRICK,
    END_BRICK,
    PRISMARINE,
    RED_SANDSTONE,
    RED_NETHER_BRICK;

    override fun getString() = name.lowercase()
}