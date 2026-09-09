package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class FillType : StringRepresentable {
    UNTIL_FULL,
    IF_GROUP_FITS;

    override fun getString(): String {
        return name.lowercase()
    }
}
