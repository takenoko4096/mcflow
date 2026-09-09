package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class PermissionLevel : StringRepresentable {
    VISITOR,
    MEMBER,
    OPERATOR;

    override fun getString(): String {
        return name.lowercase()
    }
}
