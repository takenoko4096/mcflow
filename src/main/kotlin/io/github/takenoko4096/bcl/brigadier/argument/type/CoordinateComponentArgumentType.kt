package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.value.location.CoordinateComponent
import io.github.takenoko4096.bcl.completer.BclCompleter
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind

class CoordinateComponentArgumentType : ArgumentType<CoordinateComponent.World>("coordinate_component", hasInlayHint = true) {
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<CoordinateComponent.World> {
        val reader = context.reader
        val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

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
        val completer = BclCompleter(context)
        val res = completer.completeComponent(
            listOf("~"),
            CompletionItemKind.Value
        )
        if (res is BclCompleter.ShouldComplete) {
            return res.items
        }
        return emptyList()
    }
}
