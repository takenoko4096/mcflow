package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.reader.Coordinate
import io.github.takenoko4096.bcl.reader.CoordinateComponent
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.sungrazer.Vector3d

class PositionArgumentType : ArgumentType<Vector3d>("position") {
    override fun parse(reader: TokenSeqReader): Argument<Vector3d> {
        val x = reader.readCoordinateComponent()
        val y = reader.readCoordinateComponent()
        val z = reader.readCoordinateComponent()
        return Coordinate.from(x, y, z)
    }
}
