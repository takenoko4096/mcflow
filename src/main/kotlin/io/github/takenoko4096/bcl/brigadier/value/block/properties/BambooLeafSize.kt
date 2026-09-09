package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class BambooLeafSize : StringRepresentable {
    NO_LEAVES,
    SMALL_LEAVES,
    LARGE_LEAVES;

    override fun getString() = name.lowercase()
}