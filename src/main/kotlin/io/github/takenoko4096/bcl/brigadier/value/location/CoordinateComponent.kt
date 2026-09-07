package io.github.takenoko4096.bcl.brigadier.value.location

sealed class CoordinateComponent(val value: Float) {
    sealed class World(value: Float) : CoordinateComponent(value)

    open class Absolute(value: Float) : World(value) {
        override fun toString(): String {
            return value.toString()
        }
    }

    class Relative(value: Float): World(value) {
        override fun toString(): String {
            return "~$value"
        }
    }

    class Local(value: Float): CoordinateComponent(value) {
        override fun toString(): String {
            return "^$value"
        }
    }
}
