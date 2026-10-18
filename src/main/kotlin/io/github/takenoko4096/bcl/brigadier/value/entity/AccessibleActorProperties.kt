package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

object AccessibleActorProperties {
    val ARMADILLO_STATE = ActorProperty.enum(
        NamespacedIdentifier.defaultNamespaced("armadillo_state"),
        "unrolled", "rolled_up", "rolled_up_peeking", "rolled_up_relaxing", "rolled_up_unrolling"
    )

    val CAN_MOVE = ActorProperty.boolean(NamespacedIdentifier.defaultNamespaced("can_move"))

    val CHEST_INTERACTION = ActorProperty.enum(
        NamespacedIdentifier.defaultNamespaced("chest_interaction"),
        "none", "take", "take_fail", "put", "put_fail"
    )

    val CLIMATE_VARIANT = ActorProperty.enum(
        NamespacedIdentifier.defaultNamespaced("climate_variant"),
        "temperate", "warm", "cold"
    )

    val CREAKING_STATE = ActorProperty.enum(
        NamespacedIdentifier.defaultNamespaced("creaking_state"),
        "neutral", "hostile_observed", "hostile_unobserved", "twitching", "crumbling"
    )

    val CREAKING_SWAYING_TICKS = ActorProperty.integer(
        NamespacedIdentifier.defaultNamespaced("creaking_swaying_ticks"),
        0..6
    )

    val HAS_INCREASED_MAX_HEALTH = ActorProperty.boolean(NamespacedIdentifier.defaultNamespaced("has_increased_max_health"))

    val HAS_NECTAR = ActorProperty.boolean(NamespacedIdentifier.defaultNamespaced("has_nectar"))

    val IS_ARMORABLE = ActorProperty.boolean(NamespacedIdentifier.defaultNamespaced("is_armorable"))

    val IS_PLAYING_IDLE_GROUND_SOUND = ActorProperty.boolean(NamespacedIdentifier.defaultNamespaced("is_playing_idle_ground_sound"))

    val IS_WAXED = ActorProperty.boolean(NamespacedIdentifier.defaultNamespaced("is_waxed"))

    val OXIDATION_LEVEL = ActorProperty.enum(
        NamespacedIdentifier.defaultNamespaced("oxidation_level"),
        "unoxidized", "exposed", "weathered", "oxidized"
    )

    val SOUND_VARIANT = ActorProperty.enum(
        NamespacedIdentifier.defaultNamespaced("sound_variant"),
        "default", "big", "cute", "grumpy", "mad", "puglin", "sad"
    )

    val WAS_UPGRADED_TO_1_21_100 = ActorProperty.boolean(NamespacedIdentifier.defaultNamespaced("was_upgraded_to_1_21_100"))

    val properties: List<ActorProperty<*>> = listOf(
        ARMADILLO_STATE,
        CAN_MOVE,
        CHEST_INTERACTION,
        CLIMATE_VARIANT,
        CREAKING_STATE,
        CREAKING_SWAYING_TICKS,
        HAS_NECTAR,
        HAS_INCREASED_MAX_HEALTH,
        IS_ARMORABLE,
        IS_PLAYING_IDLE_GROUND_SOUND,
        IS_WAXED,
        OXIDATION_LEVEL,
        SOUND_VARIANT,
        WAS_UPGRADED_TO_1_21_100
    )

    fun getProperty(identifier: NamespacedIdentifier): ActorProperty<*>? {
        return properties.find { it.identifier == identifier }
    }
}
