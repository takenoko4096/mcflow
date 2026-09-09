package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class MinecraftVerticalHalf : StringRepresentable {
    BOTTOM,
    TOP;

    override fun getString() = name.lowercase()
}