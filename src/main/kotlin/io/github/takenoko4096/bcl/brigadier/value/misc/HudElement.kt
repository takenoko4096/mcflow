package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class HudElement : StringRepresentable {
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

    override fun getString(): String {
        return name.lowercase()
    }
}
