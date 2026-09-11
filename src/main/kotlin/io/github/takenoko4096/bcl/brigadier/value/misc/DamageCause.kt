package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class DamageCause : CommandEnumerable {
    PISTON,
    LAVA,
    CAMPFIRE,
    FIRE,
    ANVIL,
    MAGMA,
    SOUL_CAMPFIRE,
    WITHER,
    FALLING_BLOCK,
    FIREWORKS,
    THORNS,
    NONE,
    CONTACT,
    SONIC_BOOM,
    OVERRIDE,
    ENTITY_ATTACK,
    PROJECTILE,
    SUFFOCATION,
    MACE_SMASH,
    FALL,
    STARVE,
    RAM_ATTACK,
    FIRE_TICK,
    STALACTITE,
    DROWNING,
    BLOCK_EXPLOSION,
    ENTITY_EXPLOSION,
    VOID,
    SELF_DESTRUCT,
    MAGIC,
    CHARGING,
    STALAGMITE,
    FLY_INTO_WALL,
    LIGHTNING,
    FREEZING,
    TEMPERATURE;

    override fun getNames(): Set<String> {
        return setOf(name.lowercase())
    }
}
