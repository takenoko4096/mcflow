package io.github.takenoko4096.bcl.brigadier.value

enum class ItemType(
    namespace: String = NamespacedIdentifier.DEFAULT_NAMESPACE,
    value: String? = null
) {
    AIR;

    val identifier = NamespacedIdentifier(namespace, value ?: name.lowercase())
}
