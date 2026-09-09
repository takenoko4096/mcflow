package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class CauldronLiquid : StringRepresentable {
    WATER,
    LAVA,
    POWDER_SNOW;

    override fun getString() = name.lowercase()
}