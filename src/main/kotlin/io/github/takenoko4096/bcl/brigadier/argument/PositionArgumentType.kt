package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.location.Coordinate
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class PositionArgumentType : ArgumentType<Coordinate>("position", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader): Argument<Coordinate> {
        val start = reader.getCurrentTokenStartPosition()
        val x = reader.readCoordinateComponent(blockCenterCorrection = true)
        val y = reader.readCoordinateComponent()
        val z = reader.readCoordinateComponent(blockCenterCorrection = true)
        val end = reader.getPreviousTokenEndPosition()
        val coordinate = Coordinate.from(x, y, z)
            ?: throw reader.exception(start..end, "ワールド座標系とローカル座標系を混在させることはできません")

        return Argument(this, coordinate, start..end)
    }
}
