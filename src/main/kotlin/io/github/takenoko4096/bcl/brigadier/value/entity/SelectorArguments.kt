package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.ItemType
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

    class HasItem(
        val item: ItemType,
        val location: ItemLocation,
        val slot: InvertibleValue<IntRange>?,
        val quantity: InvertibleValue<IntRange>?,
        val data: Int?
    ) {
        enum class ItemLocation {

        }
    }

    class HasPermission {
        private val map = mutableMapOf<Permission, PermissionState>()

        enum class Permission(private val string: String) : StringRepresentable {
            CAMERA("camera"),
            DISMOUNT("dismount"),
            JUMP("jump"),
            LATERAL_MOVEMENT("lateral_movement"),
            MOUNT("mount"),
            MOVE_BACKWARD("move_backward"),
            MOVE_FORWARD("move_forward"),
            MOVE_LEFT("move_left"),
            MOVE_RIGHT("move_right"),
            MOVEMENT("movement"),
            SNEAK("sneak");

            override fun getString(): String {
                return string
            }

            companion object {
                fun parse(name: String): Permission? {
                    return entries.find { it.getString() == name }
                }
            }
        }

        enum class PermissionState : StringRepresentable {
            ENABLED {
                override fun getString() = "enabled"
            },
            DISABLED {
                override fun getString() = "disabled"
            };

            companion object {
                fun parse(name: String): PermissionState? {
                    return entries.find { it.getString() == name }
                }
            }
        }

        fun set(permission: Permission, state: PermissionState) {
            map[permission] = state
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

        fun isEmpty(): Boolean = list.isEmpty()
    }
}
