package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.value.item.ItemType
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class ItemTypeArgumentType : ArgumentType<ItemType>("item_type") {
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<ItemType> {
        val reader = context.reader
        val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

        val identifier = reader.unfatalize(diagnostics) { readNamespacedIdentifier() }

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, identifier.sourceSpan))

        if (identifier !is TokenSeqReader.ReadResult.Successful) {
            return NoValuedArgument(this, identifier.sourceSpan, diagnostics)
        }

        for (entry in ItemType.entries) {
            if (identifier.value == entry.identifier) {
                return Argument(this, entry, identifier.sourceSpan)
            }
        }

        diagnostics.add(reader.diagnostic(identifier.sourceSpan, "不明なアイテムタイプです: ${identifier.value}"))
        return NoValuedArgument(this, identifier.sourceSpan, diagnostics)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return ItemType.IDENTIFIERS
            .filter { it.startsWith(context.string) }
            .map {
                CompletionItem(it).apply {
                    textEdit = context.sourceSpan?.run {
                        Either.forLeft(TextEdit(toLsp4jRange(), it))
                    }
                    kind = CompletionItemKind.EnumMember
                }
            }
    }
}
