package io.github.takenoko4096.bcl.brigadier.value

class BlockProperties : Iterable<BlockProperty.Value<*>> {
    private val entries = mutableSetOf<BlockProperty.Value<*>>()

    operator fun get(property: BlockProperty<*>): BlockProperty.Value<*>? {
        return entries.find { it.property == property }
    }

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

    override fun iterator(): Iterator<BlockProperty.Value<*>> {
        return entries.toSet().iterator()
    }

    companion object {
        val AGE = BlockProperty.integer("age", 0..15)

        val GROWTH = BlockProperty.integer("growth", 0..7)

        fun getProperty(name: String): BlockProperty<*>? {
            return BlockType.properties.find { it.name == name }
        }
    }
}
