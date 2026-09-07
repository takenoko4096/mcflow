package io.github.takenoko4096.bcl.brigadier.value

enum class BlockType(
    private val namespace: String = NamespacedIdentifier.MINECRAFT,
    private val value: String? = null,
    private val properties: Set<BlockProperty<*>> = setOf()
) : IdentifierRepresentable {
    AIR,

    POTATOES(BlockProperties.GROWTH);

    constructor(vararg properties: BlockProperty<*>) : this(properties = properties.toSet())

    override fun getIdentifier() = NamespacedIdentifier(namespace, value ?: name.lowercase())

    fun hasProperty(property: BlockProperty<*>): Boolean {
        return property in properties
    }

    companion object {
        val properties: Set<BlockProperty<*>> = entries.flatMap { it.properties }.toSet()
    }
}
