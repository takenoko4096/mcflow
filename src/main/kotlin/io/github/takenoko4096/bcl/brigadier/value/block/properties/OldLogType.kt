package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class OldLogType : StringRepresentable {
    OAK,
    SPRUCE,
    BIRCH,
    JUNGLE;

    override fun getString() = name.lowercase()
}