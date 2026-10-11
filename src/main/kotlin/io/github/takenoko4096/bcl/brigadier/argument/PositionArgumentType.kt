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
        val start = reader.getCurrentTokenStartPosition()
        val x = reader.unfatalize(diagnostics) { readCoordinateComponent(blockCenterCorrection = true) }
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, x.sourceSpan))
        val y = reader.unfatalize(diagnostics) { readCoordinateComponent() }
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, y.sourceSpan))
        val z = reader.unfatalize(diagnostics) { readCoordinateComponent(blockCenterCorrection = true) }
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, z.sourceSpan))
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
