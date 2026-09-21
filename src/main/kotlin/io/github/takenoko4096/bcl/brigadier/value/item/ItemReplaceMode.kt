package io.github.takenoko4096.bcl.brigadier.value.item

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class ItemReplaceMode : StringRepresentable, CommandEnumerable {
    DESTROY,
    KEEP;

    override fun getString() = name.lowercase()

    override fun getNames() = setOf(getString())
}
