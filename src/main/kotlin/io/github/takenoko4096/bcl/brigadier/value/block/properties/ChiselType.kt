package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class ChiselType : StringRepresentable {
    DEFAULT,
    CHISELED,
    LINES,
    SMOOTH;

    override fun getString() = name.lowercase()
}