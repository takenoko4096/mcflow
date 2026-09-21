package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class StructureAnimationMode : StringRepresentable, CommandEnumerable {
    BLOCK_BY_BLOCK,
    LAYER_BY_LAYER;

    override fun getString() = name.lowercase()

    override fun getNames() = setOf(getString())
}
