package io.github.takenoko4096.bcl.brigadier.value

enum class ItemType(
    namespace: String = NamespacedIdentifier.MINECRAFT,
    value: String? = null
) {
    AIR;

    val identifier = NamespacedIdentifier(namespace, value ?: name.lowercase())
}
