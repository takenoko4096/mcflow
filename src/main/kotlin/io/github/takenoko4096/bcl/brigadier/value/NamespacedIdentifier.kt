package io.github.takenoko4096.bcl.brigadier.value

data class NamespacedIdentifier(val namespace: String, val value: String) {
    override fun toString(): String {
        return "$namespace:$value"
    }

    companion object {
        fun parse(string: String, defaultNamespace: String? = DEFAULT_NAMESPACE): NamespacedIdentifier? {
            val result = REGEX.matchEntire(string)
                ?: return null

            val namespace = result.groupValues[1].ifEmpty {
                defaultNamespace ?: throw IllegalArgumentException("名前空間が必要です")
            }
            val path = result.groupValues[2]

            return NamespacedIdentifier(namespace, path)
        }

        val REGEX = "^(?:([a-zA-Z_\\-.]+[a-zA-Z0-9_\\-.]*):)?([a-zA-Z_\\-.]+[a-zA-Z0-9_\\-.]*)$".toRegex()

        const val DEFAULT_NAMESPACE = "minecraft"
    }
}
