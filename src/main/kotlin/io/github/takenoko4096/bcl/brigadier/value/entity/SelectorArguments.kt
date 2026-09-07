package io.github.takenoko4096.bcl.brigadier.value.entity

class SelectorArguments(callback: SelectorArguments.() -> Unit) {
    var c: Int? = null

    var dx: Float? = null
    var dy: Float? = null
    var dz: Float? = null

    val family = mutableListOf<InvertibleValue<EntityFamily>>()

    val hasProperty = mutableMapOf<String, List<InvertibleValue<Boolean>>>()

    val hasItem = mutableListOf<HasItem>()

    val type = mutableListOf<InvertibleValue<EntityType>>()

    init {
        callback()
    }

    class HasItem {

    }

    class HasPermission
}
