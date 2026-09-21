package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.location.CoordinateComponent
import io.github.takenoko4096.bcl.brigadier.value.location.Rotation
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.TokenType

// TODO pitch 省略時の後続引数記述制約を再現できていない -> RotationArgumentType は削除したほうが懸命？
class RotationArgumentType : ArgumentType<Rotation>("rotation") {
    override fun parse(reader: TokenSeqReader): Argument<Rotation> {
        val start = reader.getCurrentTokenStartPosition()
        val yaw = reader.readCoordinateComponent(false)

        if (reader.isOver() || reader.peek().type !in setOf(TokenType.INT, TokenType.FLOAT, TokenType.COORDINATE_LOCAL)) {
            val end = reader.getPreviousTokenEndPosition()

            if (yaw is CoordinateComponent.World) {
                return Argument(this, Rotation(yaw, CoordinateComponent.Relative(0f)), start..end)
            }
            else {
                throw reader.exception(start..end, "回転成分の指定にはローカル座標系を用いることができません")
            }
        }

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
