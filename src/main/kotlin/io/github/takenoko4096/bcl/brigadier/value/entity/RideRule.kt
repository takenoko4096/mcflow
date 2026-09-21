package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class RideRule : StringRepresentable, CommandEnumerable {
    NO_RIDE_CHANGE,
    REASSIGN_RIDES,
    SKIP_RIDERS;

    override fun getString() = name.lowercase()

    override fun getNames() = setOf(getString())
}
