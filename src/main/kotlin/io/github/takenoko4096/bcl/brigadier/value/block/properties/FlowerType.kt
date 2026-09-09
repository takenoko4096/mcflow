package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class FlowerType : StringRepresentable {
    POPPY,
    ORCHID,
    ALLIUM,
    HOUSTONIA,
    TULIP_RED,
    TULIP_ORANGE,
    TULIP_WHITE,
    TULIP_PINK,
    OXEYE,
    CORNFLOWER,
    LILY_OF_THE_VALLEY;

    override fun getString() = name.lowercase()
}