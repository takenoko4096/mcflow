package io.github.takenoko4096.bcl.brigadier.value

class BlockProperties {
    private val entries = mutableSetOf<BlockProperty.Value<*>>()

    /**
     * @throws IllegalArgumentException
     */
    operator fun set(property: BlockProperty<*>, value: BlockProperty.Value<*>) {
        entries.removeIf { it.property == property }
        entries.add(value)
    }

    override fun toString(): String {
        return "[${entries.joinToString(", ")}]"
    }

    operator fun contains(property: BlockProperty<*>): Boolean {
        return entries.any { it.property == property }
    }

    companion object {
        private val properties = mutableSetOf<BlockProperty<*>>()

        val AGE = BlockProperty.integer("age", 0..15)

        init {
            properties.addAll(listOf(
                AGE,

            ))
        }

        fun getProperty(name: String): BlockProperty<*>? {
            return properties.find { it.name == name }
        }
    }
}
