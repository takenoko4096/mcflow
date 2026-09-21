package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class StructureRotation : StringRepresentable, CommandEnumerable {
    `0_DEGREES`,
    `90_DEGREES`,
    `180_DEGREES`,
    `270_DEGREES`;

    override fun getString() = name.lowercase()

    override fun getNames() = setOf(getString())
}
