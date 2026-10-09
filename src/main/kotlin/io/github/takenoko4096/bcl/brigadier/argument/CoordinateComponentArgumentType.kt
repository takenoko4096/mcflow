package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.location.CoordinateComponent
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind

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

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        val completer = ArgumentCompleter(context)
        val res = completer.completeComponent(
            listOf("~"),
            CompletionItemKind.Value
        )
        if (res is ArgumentCompleter.ShouldComplete) {
            return res.items
        }
        return emptyList()
    }
}
