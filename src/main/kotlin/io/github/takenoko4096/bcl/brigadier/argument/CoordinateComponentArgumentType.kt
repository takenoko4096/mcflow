package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.value.location.CoordinateComponent
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class CoordinateComponentArgumentType : ArgumentType<CoordinateComponent.World>("coordinate_component", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader): Argument<CoordinateComponent.World> {
        val start = reader.getCurrentTokenStartPosition()
        val component = reader.readCoordinateComponent(blockCenterCorrection = true, allowLocal = false) as CoordinateComponent.World
        val end = reader.getPreviousTokenEndPosition()
        return Argument(this, component, start..end)
    }
}
