package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class ExportTypes : StringRepresentable {
    PROJECT,
    TEMPLATE,
    WORLD;

    override fun getString(): String {
        return name.lowercase()
    }
}
