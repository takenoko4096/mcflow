package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class DoublePlantType : StringRepresentable {
    SUNFLOWER,
    SYRINGA,
    GRASS,
    FERN,
    ROSE,
    PAEONIA;

    override fun getString() = name.lowercase()
}