package io.github.takenoko4096.bcl.brigadier.value.entity

data class SelectorArguments(
    var c: Int? = null,
    var dx: Float? = null,
    var dy: Float? = null,
    var dz: Float? = null,
    val family: MutableList<InvertibleValue<EntityFamily>> = mutableListOf(),
    val hasProperty: MutableMap<String, MutableList<InvertibleValue<Boolean>>> = mutableMapOf(),
    val hasItem: MutableList<HasItem> = mutableListOf(),
    val name: MutableList<InvertibleValue<String>> = mutableListOf(),
    val type: MutableList<InvertibleValue<EntityType>> = mutableListOf()
) {
    class HasItem {

    }

    class HasPermission
}
