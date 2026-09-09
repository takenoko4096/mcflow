package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class ServerIdentityAction : StringRepresentable {
    SAVE,
    DELETE,
    STATUS;

    override fun getString(): String {
        return name.lowercase()
    }
}
