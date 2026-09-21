package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class StructureMirror : StringRepresentable, CommandEnumerable {
    X,
    Z,
    NONE,
    XZ;

    override fun getString() = name.lowercase()

    override fun getNames() = setOf(getString())
}
