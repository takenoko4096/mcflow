package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class VehicleFillType : StringRepresentable, CommandEnumerable {
    UNTIL_FULL,
    IF_GROUP_FITS;

    override fun getString() = name.lowercase()

    override fun getNames() = setOf(getString())
}
