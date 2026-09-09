package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class IntGameRule : StringRepresentable {
    MAXCOMMANDCHAINLENGTH,
    RANDOMTICKSPEED,
    FUNCTIONCOMMANDLIMIT,
    SPAWNRADIUS,
    PLAYERSSLEEPINGPERCENTAGE;

    override fun getString(): String {
        return name.lowercase()
    }
}
