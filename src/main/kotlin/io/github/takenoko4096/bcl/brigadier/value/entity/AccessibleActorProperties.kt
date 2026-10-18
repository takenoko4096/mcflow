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
}
