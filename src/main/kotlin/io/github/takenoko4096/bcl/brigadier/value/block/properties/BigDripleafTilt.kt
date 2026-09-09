package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class BigDripleafTilt : StringRepresentable {
    NONE,
    UNSTABLE,
    PARTIAL_TILT,
    FULL_TILT;

    override fun getString() = name.lowercase()
}