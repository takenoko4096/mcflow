package io.github.takenoko4096.bcl.brigadier.value.item

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

data class ItemNbtComponents(
    val components: Map<NamespacedIdentifier, ItemNbtComponent>
) {
    override fun toString(): String {
        return components.toString()
    }

    companion object {
        val CAN_PLACE_ON = NamespacedIdentifier(NamespacedIdentifier.DEFAULT_NAMESPACE, "can_place_on")
        val CAN_DESTROY = NamespacedIdentifier(NamespacedIdentifier.DEFAULT_NAMESPACE, "can_destroy")
        val ITEM_LOCK = NamespacedIdentifier(NamespacedIdentifier.DEFAULT_NAMESPACE, "item_lock")
        val KEEP_ON_DEATH = NamespacedIdentifier(NamespacedIdentifier.DEFAULT_NAMESPACE, "keep_on_death")
    }
}
