package io.github.takenoko4096.bcl.brigadier.value.block

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

enum class BlockType(
    namespace: String = NamespacedIdentifier.DEFAULT_NAMESPACE,
    value: String? = null,
    private val properties: Set<BlockProperty<*>> = setOf()
) {
    AIR,

    POTATOES(BlockProperties.GROWTH);

    constructor(vararg properties: BlockProperty<*>) : this(properties = properties.toSet())

    val identifier: NamespacedIdentifier = NamespacedIdentifier(namespace, value ?: name.lowercase())

    fun hasProperty(property: BlockProperty<*>): Boolean {
        return property in properties
    }

    companion object {
        val properties: Set<BlockProperty<*>> = entries.flatMap { it.properties }.toSet()
    }
}
