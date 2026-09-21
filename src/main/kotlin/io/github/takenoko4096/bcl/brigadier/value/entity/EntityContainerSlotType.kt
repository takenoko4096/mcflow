package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class EntityContainerSlotType(private val string: String) : StringRepresentable, CommandEnumerable {
    ARMOR("slot.armor"),
    ARMOR_HEAD("slot.armor.head"),
    ARMOR_CHEST("slot.armor.chest"),
    ARMOR_LEGS("slot.armor.legs"),
    ARMOR_FEET("slot.armor.feet"),
    ARMOR_BODY("slot.armor.body"),
    WEAPON("slot.weapon"),
    WEAPON_MAINHAND("slot.weapon.mainhand"),
    WEAPON_OFFHAND("slot.weapon.offhand"),
    SADDLE("slot.saddle"),
    CHEST("slot.chest"),
    ENDER_CHEST("slot.enderchest"),
    EQUIPPABLE("slot.equippable"),
    INVENTORY("slot.inventory"),
    HOTBAR("slot.hotbar");

    override fun getString(): String {
        return string
    }

    override fun getNames(): Set<String> {
        return setOf(getString())
    }

    companion object {
        fun parse(string: String): EntityContainerSlotType? {
            return entries.find { it.getString() == string }
        }
    }
}
