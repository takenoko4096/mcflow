package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.location.Coordinate
import io.github.takenoko4096.bcl.brigadier.value.location.CoordinateComponent
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind

class PositionArgumentType : ArgumentType<Coordinate>("position", hasInlayHint = true) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<Coordinate> {
        val startX = reader.getCurrentTokenStartPosition()
        val x = reader.readCoordinateComponent(blockCenterCorrection = true)
        val endX = reader.getPreviousTokenEndPosition()
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, startX..endX))
        val startY = reader.getCurrentTokenStartPosition()
        val y = reader.readCoordinateComponent()
        val endY = reader.getPreviousTokenEndPosition()
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, startY..endY))
        val startZ = reader.getCurrentTokenStartPosition()
        val z = reader.readCoordinateComponent(blockCenterCorrection = true)
        val endZ = reader.getPreviousTokenEndPosition()
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, startZ..endZ))

        val coordinate = Coordinate.from(x, y, z)
            ?: run {
                diagnostics.add(reader.diagnostic(startX..endZ, "ワールド座標系とローカル座標系を混在させることはできません"))
                return NoValuedArgument(this, startX..endZ, diagnostics)
            }

        return Argument(this, coordinate, startX..endZ)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        val completer = ArgumentCompleter(context)
        val res = completer.completeCoordinate(listOf(
            Coordinate.from(
                CoordinateComponent.Absolute.ZERO,
                CoordinateComponent.Absolute.ZERO,
                CoordinateComponent.Absolute.ZERO
            )!!,
            Coordinate.from(
                CoordinateComponent.Relative.ZERO,
                CoordinateComponent.Relative.ZERO,
                CoordinateComponent.Relative.ZERO
            )!!,
            Coordinate.from(
                CoordinateComponent.Local.ZERO,
                CoordinateComponent.Local.ZERO,
                CoordinateComponent.Local.ZERO
            )!!
        ).map(Coordinate::toString), CompletionItemKind.Value)
        if (res is ArgumentCompleter.ShouldComplete) {
            return res.items
        }
        return emptyList()
    }
}
