package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class EntityEquipmentSlot : StringRepresentable {
    SLOT.WEAPON.MAINHAND,
    SLOT.WEAPON.OFFHAND,
    SLOT.ARMOR.HEAD,
    SLOT.ARMOR.CHEST,
    SLOT.ARMOR.LEGS,
    SLOT.ARMOR.FEET,
    SLOT.ARMOR.BODY,
    SLOT.HOTBAR,
    SLOT.INVENTORY,
    SLOT.ENDERCHEST,
    SLOT.SADDLE,
    SLOT.ARMOR,
    SLOT.CHEST,
    SLOT.EQUIPPABLE;

    override fun getString(): String {
        return name.lowercase()
    }
}
