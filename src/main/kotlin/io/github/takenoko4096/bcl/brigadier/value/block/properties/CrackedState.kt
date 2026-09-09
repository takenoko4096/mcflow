package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class CrackedState : StringRepresentable {
    NO_CRACKS,
    CRACKED,
    MAX_CRACKED;

    override fun getString() = name.lowercase()
}