package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class NewLeafType : StringRepresentable {
    ACACIA,
    DARK_OAK;

    override fun getString() = name.lowercase()
}