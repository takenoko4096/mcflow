package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class RideRules : StringRepresentable {
    NO_RIDE_CHANGE,
    REASSIGN_RIDES,
    SKIP_RIDERS;

    override fun getString(): String {
        return name.lowercase()
    }
}
