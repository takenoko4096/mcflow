package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class CameraPresets : StringRepresentable {
    MINECRAFT:FIRST_PERSON,
    MINECRAFT:FIXED_BOOM,
    MINECRAFT:FOLLOW_ORBIT,
    MINECRAFT:FREE,
    MINECRAFT:THIRD_PERSON,
    MINECRAFT:THIRD_PERSON_FRONT,
    MINECRAFT:CONTROL_SCHEME_CAMERA;

    override fun getString(): String {
        return name.lowercase()
    }
}
