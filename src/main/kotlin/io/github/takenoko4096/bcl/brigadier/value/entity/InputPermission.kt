package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class InputPermission : CommandEnumerable {
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

    override fun getNames(): Set<String> {
        return setOf(name.lowercase())
    }
}
