package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class IntGameRule(val range: IntRange) : StringRepresentable, CommandEnumerable {
    MAXCOMMANDCHAINLENGTH(Int.MIN_VALUE..Int.MAX_VALUE),
    RANDOMTICKSPEED(0..4096),
    FUNCTIONCOMMANDLIMIT(0..10000),
    SPAWNRADIUS(0..128),
    PLAYERSSLEEPINGPERCENTAGE(Int.MIN_VALUE..Int.MAX_VALUE);

    override fun getString(): String {
        return name.lowercase()
    }

    override fun getNames(): Set<String> {
        return setOf(getString())
    }
}
