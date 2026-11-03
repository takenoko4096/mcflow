package io.github.takenoko4096.bcl.brigadier.value.item

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

data class ItemType(
    val identifier: NamespacedIdentifier,
    val isVanilla: Boolean
)
