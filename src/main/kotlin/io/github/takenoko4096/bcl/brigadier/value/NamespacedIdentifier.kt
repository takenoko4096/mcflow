package io.github.takenoko4096.bcl.brigadier.value

data class NamespacedIdentifier(val namespace: String, val value: String) {
    override fun toString(): String {
        return "$namespace:$value"
    }

    companion object {
        const val DEFAULT_NAMESPACE = "minecraft"
    }
}
