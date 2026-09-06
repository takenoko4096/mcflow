package io.github.takenoko4096.bcl.brigadier.value

class BlockProperties {
    private val entries = mutableSetOf<Entry<*>>()

    operator fun <T> get(property: BlockProperty<T>): T? {
        return entries.find { it.property == property }?.value as T?
    }

    operator fun <T> set(property: BlockProperty<T>, value: T) {
        entries.removeIf { it.property == property }
        entries.add(Entry(property, value))
    }

    override fun toString(): String {
        return "[${entries.joinToString(", ")}]"
    }

    class Entry<T>(val property: BlockProperty<T>, val value: T) {
        override fun toString(): String {
            return "$property=$value"
        }
    }
}
