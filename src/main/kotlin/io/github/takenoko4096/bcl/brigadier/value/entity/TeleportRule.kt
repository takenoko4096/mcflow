package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class TeleportRule : StringRepresentable, CommandEnumerable {
    TELEPORT_RIDER,
    TELEPORT_RIDE;

    override fun getString() = name.lowercase()

    override fun getNames() = setOf(getString())
}
