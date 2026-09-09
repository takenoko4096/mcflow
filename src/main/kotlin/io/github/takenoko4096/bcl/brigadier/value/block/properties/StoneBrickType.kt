package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class StoneBrickType : StringRepresentable {
    DEFAULT,
    MOSSY,
    CRACKED,
    CHISELED,
    SMOOTH;

    override fun getString() = name.lowercase()
}