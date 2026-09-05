package io.github.takenoko4096.bcl.reader

sealed class Coordinate(
    val x: CoordinateComponent,
    val y: CoordinateComponent,
    val z: CoordinateComponent
){
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
        fun from(x: CoordinateComponent, y: CoordinateComponent, z: CoordinateComponent): Coordinate {
            if (x is CoordinateComponent.World && y is CoordinateComponent.World && z is CoordinateComponent.World) {

            }
            else if (x is CoordinateComponent.Local && y is CoordinateComponent.Local && z is CoordinateComponent.Local) {

            }
            else {

            }
        }
    }
}
