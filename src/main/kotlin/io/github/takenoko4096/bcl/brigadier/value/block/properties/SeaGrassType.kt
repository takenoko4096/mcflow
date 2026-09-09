package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class SeaGrassType : StringRepresentable {
    DEFAULT,
    DOUBLE_TOP,
    DOUBLE_BOT;

    override fun getString() = name.lowercase()
}