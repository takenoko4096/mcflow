package io.github.takenoko4096.bcl.brigadier.value

enum class ItemType(private val namespace: String = NamespacedIdentifier.MINECRAFT, private val value: String? = null) : IdentifierRepresentable {
    AIR;

    override fun getIdentifier() = NamespacedIdentifier(namespace, value ?: name.lowercase())
}
