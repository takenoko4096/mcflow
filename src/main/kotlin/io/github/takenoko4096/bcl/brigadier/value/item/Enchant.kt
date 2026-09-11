package io.github.takenoko4096.bcl.brigadier.value.item

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Enchant(minLevel: Int, maxLevel: Int) : StringRepresentable, CommandEnumerable {
    PROTECTION(1, 4),
    FIRE_PROTECTION(1, 4),
    FEATHER_FALLING(1, 4),
    BLAST_PROTECTION(1, 4),
    PROJECTILE_PROTECTION(1, 4),
    THORNS(1, 3),
    RESPIRATION(1, 1),
    DEPTH_STRIDER(1, 3),
    AQUA_AFFINITY(1, 1),
    SHARPNESS(1, 5),
    SMITE(1, 5),
    BANE_OF_ARTHROPODS(1, 5),
    KNOCKBACK(1, 2),
    FIRE_ASPECT(1, 2),
    LOOTING(1, 3),
    EFFICIENCY(1, 5),
    SILK_TOUCH(1, 1),
    UNBREAKING(1, 3),
    FORTUNE(1, 3),
    POWER(1, 5),
    PUNCH(1, 2),
    FLAME(1, 1),
    INFINITY(1, 1),
    LUCK_OF_THE_SEA(1, 3),
    LURE(1, 3),
    FROST_WALKER(1, 2),
    MENDING(1, 1),
    BINDING(1, 1),
    VANISHING(1, 1),
    IMPALING(1, 5),
    RIPTIDE(1, 3),
    LOYALTY(1, 3),
    CHANNELING(1, 1),
    MULTISHOT(1, 1),
    PIERCING(1, 4),
    QUICK_CHARGE(1, 3),
    SOUL_SPEED(1, 3),
    SWIFT_SNEAK(1, 3),
    WIND_BURST(1, 3),
    DENSITY(1, 5),
    BREACH(1, 4),
    LUNGE(1, 3);

    val levels = minLevel..maxLevel

    override fun getString(): String {
        return name.lowercase()
    }

    override fun getNames(): Set<String> {
        return setOf(getString(), ordinal.toString())
    }
}
