package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class StructureSaveMode : StringRepresentable, CommandEnumerable {
    DISK,
    MEMORY;

    override fun getString() = name.lowercase()

    override fun getNames() = setOf(getString())
}
