package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class DripstoneThickness : StringRepresentable {
    TIP,
    FRUSTUM,
    MIDDLE,
    BASE,
    MERGE;

    override fun getString() = name.lowercase()
}