package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class StructureVoidType : StringRepresentable {
    VOID,
    AIR;

    override fun getString() = name.lowercase()
}