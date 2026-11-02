package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

data class EntityType(
    val identifier: NamespacedIdentifier,
    val isSummonable: Boolean,
    val isVanilla: Boolean
)
