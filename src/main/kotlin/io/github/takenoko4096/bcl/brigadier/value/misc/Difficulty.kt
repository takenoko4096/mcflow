package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.AliasRepresentable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Difficulty(private val string: String?, private val int: Int?) : StringRepresentable, AliasRepresentable {
    PEACEFUL("p", 0),
    EASY("e", 1),
    NORMAL("n", 2),
    HARD("h", 3);

    override fun getString(): String {
        return name.lowercase()
    }

    override fun getAliases(): Set<String> {
        return setOfNotNull(getString(), string, int?.toString())
    }
}
