package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class StoneSlabType2 : StringRepresentable {
    RED_SANDSTONE,
    PURPUR,
    PRISMARINE_ROUGH,
    PRISMARINE_DARK,
    PRISMARINE_BRICK,
    MOSSY_COBBLESTONE,
    SMOOTH_SANDSTONE,
    RED_NETHER_BRICK;

    override fun getString() = name.lowercase()
}