package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class Difficulty(private val alias: String?, private val id: Int) : CommandEnumerable {
    PEACEFUL("p", 0),
    EASY("e", 1),
    NORMAL("n", 2),
    HARD("h", 3);

    override fun getNames(): Set<String> {
        return setOfNotNull(name.lowercase(), alias, id.toString())
    }
}
