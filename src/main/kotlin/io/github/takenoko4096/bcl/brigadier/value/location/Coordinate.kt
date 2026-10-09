package io.github.takenoko4096.bcl.brigadier.value.location

sealed class Coordinate(
    val x: CoordinateComponent,
    val y: CoordinateComponent,
    val z: CoordinateComponent
){
    override fun toString(): String {
        return "$x $y $z"
    }

    class InWorld(
        x: CoordinateComponent.World,
        y: CoordinateComponent.World,
        z: CoordinateComponent.World
    ) : Coordinate(x, y, z)

    class InLocal(
        x: CoordinateComponent.Local,
        y: CoordinateComponent.Local,
        z: CoordinateComponent.Local
    ) : Coordinate(x, y, z)

    companion object {
        fun from(x: CoordinateComponent, y: CoordinateComponent, z: CoordinateComponent): Coordinate? {
            return when (x) {
                is CoordinateComponent.World if y is CoordinateComponent.World && z is CoordinateComponent.World -> {
                    InWorld(x, y, z)
                }
                is CoordinateComponent.Local if y is CoordinateComponent.Local && z is CoordinateComponent.Local -> {
                    InLocal(x, y, z)
                }
                else -> null
            }
        }
    }
}
