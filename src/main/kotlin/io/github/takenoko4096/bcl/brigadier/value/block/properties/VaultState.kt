package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class VaultState : StringRepresentable {
    INACTIVE,
    ACTIVE,
    UNLOCKING,
    EJECTING;

    override fun getString() = name.lowercase()
}