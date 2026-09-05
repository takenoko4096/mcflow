package io.github.takenoko4096.bcl.reader

sealed class CoordinateComponent(private val value: Float) {
    sealed class World(value: Float) : CoordinateComponent(value)

    class Absolute(value: Float) : World(value)

    class Relative(value: Float): World(value)

    class Local(value: Float): CoordinateComponent(value)
}
