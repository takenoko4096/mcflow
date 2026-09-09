package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class PotentSulfurState : StringRepresentable {
    DRY,
    WET,
    DORMANT,
    ERUPTING,
    CONTINUOUS;

    override fun getString() = name.lowercase()
}