package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class ControlScheme : CommandEnumerable {
    CAMERA_RELATIVE_STRAFE,
    CAMERA_RELATIVE,
    PLAYER_RELATIVE_STRAFE,
    PLAYER_RELATIVE,
    LOCKED_PLAYER_RELATIVE_STRAFE;

    override fun getNames(): Set<String> {
        return setOf(name.lowercase())
    }
}
