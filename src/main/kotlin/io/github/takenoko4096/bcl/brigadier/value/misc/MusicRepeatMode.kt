package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class MusicRepeatMode : StringRepresentable {
    PLAY_ONCE,
    LOOP;

    override fun getString(): String {
        return name.lowercase()
    }
}
