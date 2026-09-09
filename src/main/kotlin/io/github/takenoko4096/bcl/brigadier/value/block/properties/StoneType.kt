package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class StoneType : StringRepresentable {
    STONE,
    GRANITE,
    GRANITE_SMOOTH,
    DIORITE,
    DIORITE_SMOOTH,
    ANDESITE,
    ANDESITE_SMOOTH;

    override fun getString() = name.lowercase()
}