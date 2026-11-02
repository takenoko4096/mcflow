package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class BooleanArgumentType : ArgumentType<Boolean>("boolean", hasInlayHint = true) {
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<Boolean> {
        val reader = context.reader
        val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

        val bool = reader.unfatalize(diagnostics) { readBool() }

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.BOOLEAN, bool.sourceSpan))

        if (bool is TokenSeqReader.ReadResult.Failure) {
            return NoValuedArgument(this, bool.sourceSpan, diagnostics)
        }

        return Argument(this, (bool as TokenSeqReader.ReadResult.Successful).value, bool.sourceSpan)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return listOf("true", "false")
            .filter { it.startsWith(context.string) }
            .map {
                CompletionItem(it).apply {
                    textEdit = context.sourceSpan?.run {
                        Either.forLeft(TextEdit(
                            toLsp4jRange(),
                            it
                        ))
                    }
                    kind = CompletionItemKind.Value
                }
            }
    }
}
