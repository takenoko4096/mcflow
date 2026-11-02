package io.github.takenoko4096.bcl.brigadier.value.block

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

data class BlockType(
    val identifier: NamespacedIdentifier,
    val properties: Set<BlockProperty<*>>,
    val isVanilla: Boolean
)
