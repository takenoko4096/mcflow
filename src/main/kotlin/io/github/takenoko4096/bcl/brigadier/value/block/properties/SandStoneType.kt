package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class SandStoneType : StringRepresentable {
    DEFAULT,
    HEIROGLYPHS,
    CUT,
    SMOOTH;

    override fun getString() = name.lowercase()
}