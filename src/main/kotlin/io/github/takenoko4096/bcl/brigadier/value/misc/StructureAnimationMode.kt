package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class StructureAnimationMode : StringRepresentable {
    BLOCK_BY_BLOCK,
    LAYER_BY_LAYER;

    override fun getString(): String {
        return name.lowercase()
    }
}
