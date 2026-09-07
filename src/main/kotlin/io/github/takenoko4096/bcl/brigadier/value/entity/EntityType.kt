package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

enum class EntityType(
    namespace: String = NamespacedIdentifier.DEFAULT_NAMESPACE,
    value: String? = null
) {
    ARMOR_STAND,
    CREEPER,
    PLAYER;

    val identifier = NamespacedIdentifier(namespace, value ?: name.lowercase())
}
