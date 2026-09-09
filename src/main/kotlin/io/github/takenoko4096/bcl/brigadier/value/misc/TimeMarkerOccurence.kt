package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class TimeMarkerOccurence : StringRepresentable {
    NEXT,
    PREVIOUS,
    STAY;

    override fun getString(): String {
        return name.lowercase()
    }
}
