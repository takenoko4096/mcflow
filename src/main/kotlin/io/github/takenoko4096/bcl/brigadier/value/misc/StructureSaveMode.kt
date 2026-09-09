package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class StructureSaveMode : StringRepresentable {
    DISK,
    MEMORY;

    override fun getString(): String {
        return name.lowercase()
    }
}
