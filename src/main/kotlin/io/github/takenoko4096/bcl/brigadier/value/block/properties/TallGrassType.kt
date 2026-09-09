package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class TallGrassType : StringRepresentable {
    DEFAULT,
    TALL,
    FERN,
    SNOW;

    override fun getString() = name.lowercase()
}