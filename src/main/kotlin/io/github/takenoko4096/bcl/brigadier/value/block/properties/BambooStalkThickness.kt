package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class BambooStalkThickness : StringRepresentable {
    THIN,
    THICK;

    override fun getString() = name.lowercase()
}