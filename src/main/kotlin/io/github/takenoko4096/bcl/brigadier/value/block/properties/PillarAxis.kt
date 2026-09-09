package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class PillarAxis : StringRepresentable {
    Y,
    X,
    Z;

    override fun getString() = name.lowercase()
}