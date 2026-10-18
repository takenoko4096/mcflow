package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.SpanHolder
import io.github.takenoko4096.bcl.WithSpan
import io.github.takenoko4096.bcl.brigadier.value.item.ItemType
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.location.CoordinateComponent
import io.github.takenoko4096.bcl.tokenizer.SourceSpan

data class SelectorArguments(
    var c: WithSpan<Int>? = null,
    var dx: WithSpan<Float>? = null,
    var dy: WithSpan<Float>? = null,
    var dz: WithSpan<Float>? = null,
    val family: MutableList<InvertibleValue<String>> = mutableListOf(),
    var hasProperty: HasProperty? = null,
    val hasItem: MutableList<HasItem> = mutableListOf(),
    var hasPermission: HasPermission? = null,
    var l: Int? = null,
    var lm: Int? = null,
    var m: InvertibleValue<GameMode>? = null,
    val name: MutableList<InvertibleValue<String>> = mutableListOf(),
    var r: Float? = null,
    var rm: Float? = null,
    var rx: Float? = null,
    var rxm: Float? = null,
    var ry: Float? = null,
    var rym: Float? = null,
    var scores: Scores? = null,
    val tag: MutableList<InvertibleValue<String?>> = mutableListOf(),
    val type: MutableList<InvertibleValue<EntityType>> = mutableListOf(),
    var x: CoordinateComponent? = null,
    var y: CoordinateComponent? = null,
    var z: CoordinateComponent? = null
) {
    fun isEmpty()
        = c == null
        && dx == null
        && dy == null
        && dz == null
        && family.isEmpty()
        && hasProperty == null
        && hasItem.isEmpty()
        && hasPermission == null
        && l == null
        && lm == null
        && m == null
        && name.isEmpty()
        && r == null
        && rm == null
        && rx == null
        && rxm == null
        && ry == null
        && rym == null
        && scores == null
        && tag.isEmpty()
        && type.isEmpty()
        && x == null
        && y == null
        && z == null

    override fun toString(): String {
        return listOfNotNull(
            c?.run { "c=$value" },
            dx?.run { "dx=$value" },
            dy?.run { "dy=$value" },
            dz?.run { "dz=$value" },
            family.ifEmpty { null }?.run { "family=$this" },
            hasProperty?.run { "has_property=$this" },
            hasItem.ifEmpty { null }?.run { "hasitem=$this" },
            hasPermission?.run { "haspermission=$this" },
            l?.run { "l=$this" },
            lm?.run { "lm=$this" },
            m?.run { "m=$this" },
            name.ifEmpty { null }?.run { "name=$this" },
            r?.run { "r=$this" },
            rm?.run { "rm=$this" },
            rx?.run { "rx=$this" },
            rxm?.run { "rxm=$this" },
            ry?.run { "ry=$this" },
            rym?.run { "rym=$this" },
            scores?.run { "scores=$this" },
            tag.ifEmpty { null }?.run { "tag=$this" },
            type.ifEmpty { null }?.run { "type=$this" },
            x?.run { "x=$this" },
            y?.run { "y=$this" },
            z?.run { "z=$this" }
        ).toString()
    }

    data class HasProperty(
        val properties: List<InvertibleValue<NamespacedIdentifier>>,
        val conditions: List<Pair<NamespacedIdentifier, InvertibleValue<String>>>,
        val sourceSpan: SourceSpan
    ) {
        override fun toString(): String {
            val s1 = conditions.joinToString(", ") { (id, value) -> "$id=$value" }
            val s2 = properties.joinToString(", ")

            return if (properties.isEmpty() && conditions.isEmpty()) {
                throw IllegalStateException("NEVER HAPPENS")
            }
            else if (properties.isEmpty()) {
                "{$s1}"
            }
            else if (conditions.isEmpty()) {
                "{property=[$s2]}"
            }
            else {
                "{$s1, property=[$s2]}"
            }
        }
    }

    data class HasItem(
        val item: ItemType,
        val location: EntityContainerSlotType?,
        val slot: InvertibleValue<IntRange>?,
        val quantity: InvertibleValue<IntRange>?,
        val data: Int?,
        override val sourceSpan: SourceSpan
    ) : SpanHolder {
        override fun toString(): String {
            var string = "item=${item.identifier}"
            if (location != null) string += ", location=${location.getString()}"
            if (slot != null) string += ", slot=$slot"
            if (quantity != null) string += ", quantity=$quantity"
            if (data != null) string += ", data=${data}"
            return "{$string}"
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
        private val list = mutableListOf<Pair<WithSpan<String>, InvertibleValue<IntRange>>>()

        fun add(objective: WithSpan<String>, range: InvertibleValue<IntRange>) {
            list.add(objective to range)
        }

        override fun toString(): String {
            return list.joinToString(", ", "{", "}") { (first, second) -> "${first.value}=$second" }
        }

        fun isEmpty(): Boolean = list.isEmpty()
    }
}
