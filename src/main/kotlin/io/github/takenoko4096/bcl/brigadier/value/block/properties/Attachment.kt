package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Attachment : StringRepresentable {
    STANDING,
    HANGING,
    SIDE,
    MULTIPLE;

    override fun getString() = name.lowercase()
}