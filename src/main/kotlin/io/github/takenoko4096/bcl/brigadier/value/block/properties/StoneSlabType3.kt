package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class StoneSlabType3 : StringRepresentable {
    END_STONE_BRICK,
    SMOOTH_RED_SANDSTONE,
    POLISHED_ANDESITE,
    ANDESITE,
    DIORITE,
    POLISHED_DIORITE,
    GRANITE,
    POLISHED_GRANITE;

    override fun getString() = name.lowercase()
}