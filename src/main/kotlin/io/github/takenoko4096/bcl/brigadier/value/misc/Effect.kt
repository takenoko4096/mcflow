package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class Effect : StringRepresentable {
    WITHER,
    SPEED,
    SLOWNESS,
    HASTE,
    MINING_FATIGUE,
    STRENGTH,
    INSTANT_HEALTH,
    INSTANT_DAMAGE,
    JUMP_BOOST,
    NAUSEA,
    REGENERATION,
    RESISTANCE,
    FIRE_RESISTANCE,
    WATER_BREATHING,
    INVISIBILITY,
    BLINDNESS,
    NIGHT_VISION,
    HUNGER,
    WEAKNESS,
    POISON,
    HEALTH_BOOST,
    ABSORPTION,
    SATURATION,
    LEVITATION,
    FATAL_POISON,
    CONDUIT_POWER,
    SLOW_FALLING,
    BAD_OMEN,
    VILLAGE_HERO,
    DARKNESS,
    TRIAL_OMEN,
    WIND_CHARGED,
    WEAVING,
    OOZING,
    INFESTED,
    RAID_OMEN,
    BREATH_OF_THE_NAUTILUS;

    override fun getString(): String {
        return name.lowercase()
    }
}
