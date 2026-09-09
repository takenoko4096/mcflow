package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Damage : StringRepresentable {
    UNDAMAGED,
    SLIGHTLY_DAMAGED,
    VERY_DAMAGED,
    BROKEN;

    override fun getString() = name.lowercase()
}