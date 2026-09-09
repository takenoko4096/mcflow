package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class TurtleEggCount : StringRepresentable {
    ONE_EGG,
    TWO_EGG,
    THREE_EGG,
    FOUR_EGG;

    override fun getString() = name.lowercase()
}