package io.github.takenoko4096.bcl.brigadier.value

data class NamespacedIdentifier(val namespace: String, val value: String) {
    companion object {
        fun minecraft(value: String): NamespacedIdentifier {
            return NamespacedIdentifier("minecraft", value)
        }
    }
}
