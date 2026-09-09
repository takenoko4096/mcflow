package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class StoneSlabType4 : StringRepresentable {
    MOSSY_STONE_BRICK,
    SMOOTH_QUARTZ,
    STONE,
    CUT_SANDSTONE,
    CUT_RED_SANDSTONE;

    override fun getString() = name.lowercase()
}