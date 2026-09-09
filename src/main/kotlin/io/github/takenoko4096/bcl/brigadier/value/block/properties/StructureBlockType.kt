package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class StructureBlockType : StringRepresentable {
    DATA,
    SAVE,
    LOAD,
    CORNER,
    INVALID,
    EXPORT;

    override fun getString() = name.lowercase()
}