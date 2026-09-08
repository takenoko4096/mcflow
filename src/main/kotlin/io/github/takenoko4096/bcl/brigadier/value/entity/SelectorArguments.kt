package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.location.CoordinateComponent

data class SelectorArguments(
    var c: Int? = null,
    var dx: Float? = null,
    var dy: Float? = null,
    var dz: Float? = null,
    val family: MutableList<InvertibleValue<String>> = mutableListOf(),
    val hasProperty: HasProperty? = null,
    val hasItem: MutableList<HasItem> = mutableListOf(),
    var hasPermission: HasPermission? = null,
    var l: Int? = null,
    var lm: Int? = null,
    var m: GameMode? = null,
    val name: MutableList<InvertibleValue<String>> = mutableListOf(),
    var r: Float? = null,
    var rm: Float? = null,
    var rx: Float? = null,
    var rxm: Float? = null,
    var ry: Float? = null,
    var rym: Float? = null,
    var scores: Scores? = null,
    val tag: MutableList<InvertibleValue<String>> = mutableListOf(),
    val type: MutableList<InvertibleValue<EntityType>> = mutableListOf(),
    var x: CoordinateComponent? = null,
    var y: CoordinateComponent? = null,
    var z: CoordinateComponent? = null
) {
    class HasProperty {

    }

    class HasItem {

    }

    class HasPermission {
        private val map = mutableMapOf<String, PermissionState>()

        enum class PermissionState : StringRepresentable {
            ENABLED {
                override fun getString() = "enabled"
            },
            DISABLED {
                override fun getString() = "disabled"
            }
        }

        fun set(name: String, state: PermissionState) {
            map[name] = state
        }

        override fun toString(): String {
            return map.toString()
        }
    }

    class Scores {
        private val list = mutableListOf<Pair<String, InvertibleValue<IntRange>>>()

        fun add(objective: String, range: InvertibleValue<IntRange>) {
            list.add(objective to range)
        }

        override fun toString(): String {
            return list.joinToString(", ", "{", "}") { (first, second) -> "$first=$second" }
        }
    }
}
