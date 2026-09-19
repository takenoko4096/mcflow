package io.github.takenoko4096.bcl.brigadier.value.block

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class ScanMode : StringRepresentable, CommandEnumerable {
    MASKED,
    ALL;

    override fun getString(): String {
        return name.lowercase()
    }

    override fun getNames(): Set<String> {
        return setOf(getString())
    }
}
