package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Permission : StringRepresentable {
    CAMERA,
    MOVEMENT,
    JUMP,
    LATERAL_MOVEMENT,
    SNEAK,
    DISMOUNT,
    MOUNT,
    MOVE_BACKWARD,
    MOVE_FORWARD,
    MOVE_LEFT,
    MOVE_RIGHT;

    override fun getString(): String {
        return name.lowercase()
    }
}
