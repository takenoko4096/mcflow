package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class MusicRepeatMode : CommandEnumerable {
    PLAY_ONCE,
    LOOP;

    override fun getNames(): Set<String> {
        return setOf(name.lowercase())
    }
}
