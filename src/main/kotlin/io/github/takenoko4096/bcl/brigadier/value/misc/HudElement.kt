package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class HudElement : CommandEnumerable {
    HUNGER,
    ALL,
    PAPERDOLL,
    ARMOR,
    TOOLTIPS,
    TOUCH_CONTROLS,
    CROSSHAIR,
    HOTBAR,
    HEALTH,
    PROGRESS_BAR,
    AIR_BUBBLES,
    HORSE_HEALTH,
    STATUS_EFFECTS,
    ITEM_TEXT;

    override fun getNames(): Set<String> {
        return setOf(name.lowercase())
    }
}
