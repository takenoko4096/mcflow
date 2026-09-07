package io.github.takenoko4096.bcl.brigadier.value.location

class Rotation(val yaw: CoordinateComponent.World, val pitch: CoordinateComponent.World) {
    override fun toString(): String {
        return "$yaw $pitch"
    }
}