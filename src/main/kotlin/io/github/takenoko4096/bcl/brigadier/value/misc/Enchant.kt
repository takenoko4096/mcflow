package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Enchant : StringRepresentable {
    PROTECTION,
    FIRE_PROTECTION,
    FEATHER_FALLING,
    BLAST_PROTECTION,
    PROJECTILE_PROTECTION,
    THORNS,
    RESPIRATION,
    DEPTH_STRIDER,
    AQUA_AFFINITY,
    SHARPNESS,
    SMITE,
    BANE_OF_ARTHROPODS,
    KNOCKBACK,
    FIRE_ASPECT,
    LOOTING,
    EFFICIENCY,
    SILK_TOUCH,
    UNBREAKING,
    FORTUNE,
    POWER,
    PUNCH,
    FLAME,
    INFINITY,
    LUCK_OF_THE_SEA,
    LURE,
    FROST_WALKER,
    MENDING,
    BINDING,
    VANISHING,
    IMPALING,
    RIPTIDE,
    LOYALTY,
    CHANNELING,
    MULTISHOT,
    PIERCING,
    QUICK_CHARGE,
    SOUL_SPEED,
    SWIFT_SNEAK,
    WIND_BURST,
    DENSITY,
    BREACH,
    LUNGE;

    override fun getString(): String {
        return name.lowercase()
    }
}
