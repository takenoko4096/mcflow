package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.location.Coordinate
import io.github.takenoko4096.bcl.reader.TokenSeqReader

class PositionArgumentType : ArgumentType<Coordinate>("position", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<Coordinate> {
        val start = reader.getCurrentTokenStartPosition()
        val x = reader.unfatalize(diagnostics) { readCoordinateComponent(blockCenterCorrection = true) }
        val y = reader.unfatalize(diagnostics) { readCoordinateComponent() }
        val z = reader.unfatalize(diagnostics) { readCoordinateComponent(blockCenterCorrection = true) }
        val end = reader.getPreviousTokenEndPosition()

        if (x !is TokenSeqReader.ReadResult.Successful || y !is TokenSeqReader.ReadResult.Successful || z !is TokenSeqReader.ReadResult.Successful) {
            return NoValuedArgument(this, start..end, diagnostics)
        }

        val coordinate = Coordinate.from(x.value, y.value, z.value)
            ?: run {
                diagnostics.add(reader.diagnostic(start..end, "ワールド座標系とローカル座標系を混在させることはできません"))
                return NoValuedArgument(this, start..end, diagnostics)
            }

        return Argument(this, coordinate, start..end)
    }
}
