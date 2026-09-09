package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class TeleportRules : StringRepresentable {
    TELEPORT_RIDER,
    TELEPORT_RIDE;

    override fun getString(): String {
        return name.lowercase()
    }
}
