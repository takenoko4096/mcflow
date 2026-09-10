package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.AliasRepresentable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class CameraPreset : StringRepresentable, AliasRepresentable {
    FIRST_PERSON,
    FIXED_BOOM,
    FOLLOW_ORBIT,
    FREE,
    THIRD_PERSON,
    THIRD_PERSON_FRONT,
    CONTROL_SCHEME_CAMERA;

    override fun getString(): String {
        return name.lowercase()
    }

    override fun getAliases(): Set<String> {
        return setOf("minecraft:${getString()}")
    }
}
