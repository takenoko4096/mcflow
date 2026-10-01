package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.location.CoordinateComponent
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType

class CoordinateComponentArgumentType : ArgumentType<CoordinateComponent.World>("coordinate_component", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<CoordinateComponent.World> {
        val start = reader.getCurrentTokenStartPosition()
        val component = reader.unfatalize(diagnostics) {
            readCoordinateComponent(blockCenterCorrection = true, allowLocal = false) as CoordinateComponent.World
        }
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start..end))

        if (component !is TokenSeqReader.ReadResult.Successful) {
            return NoValuedArgument(this, start..end, diagnostics)
        }

        return Argument(this, component.value, start..end)
    }
}
