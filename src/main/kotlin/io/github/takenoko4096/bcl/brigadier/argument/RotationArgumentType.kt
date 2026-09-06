package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.CoordinateComponent
import io.github.takenoko4096.bcl.brigadier.value.Rotation
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class RotationArgumentType : ArgumentType<Rotation>("rotation") {
    override fun parse(reader: TokenSeqReader): Argument<Rotation> {
        val start = reader.getCurrentTokenStartPosition()
        val yaw = reader.readCoordinateComponent(false)
        val pitch = reader.readCoordinateComponent(false)
        val end = reader.getPreviousTokenEndPosition()

        if (yaw is CoordinateComponent.World && pitch is CoordinateComponent.World) {
            return Argument(this, Rotation(yaw, pitch), start..end)
        }
        else {
            throw reader.exception(start..end, "回転成分の指定にはローカル座標系を用いることができません")
        }
    }
}
