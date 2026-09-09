package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class SandType : StringRepresentable {
    NORMAL,
    RED;

    override fun getString() = name.lowercase()
}