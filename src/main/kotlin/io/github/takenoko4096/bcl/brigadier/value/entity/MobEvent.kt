package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class MobEvent : StringRepresentable, CommandEnumerable {
    `MINECRAFT:PILLAGER_PATROLS_EVENT`,
    `MINECRAFT:WANDERING_TRADER_EVENT`,
    `MINECRAFT:ENDER_DRAGON_EVENT`,
    EVENTS_ENABLED;

    override fun getString(): String {
        return name.lowercase()
    }

    override fun getNames(): Set<String> {
        return setOf(getString())
    }
}
