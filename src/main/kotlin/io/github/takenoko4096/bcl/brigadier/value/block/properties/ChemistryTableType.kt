package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class ChemistryTableType : StringRepresentable {
    COMPOUND_CREATOR,
    MATERIAL_REDUCER,
    ELEMENT_CONSTRUCTOR,
    LAB_TABLE;

    override fun getString() = name.lowercase()
}