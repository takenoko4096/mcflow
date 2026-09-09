package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class MobEvent : StringRepresentable {
    MINECRAFT:PILLAGER_PATROLS_EVENT,
    MINECRAFT:WANDERING_TRADER_EVENT,
    MINECRAFT:ENDER_DRAGON_EVENT,
    EVENTS_ENABLED;

    override fun getString(): String {
        return name.lowercase()
    }
}
