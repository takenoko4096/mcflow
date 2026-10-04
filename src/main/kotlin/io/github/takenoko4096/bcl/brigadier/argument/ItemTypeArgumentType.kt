package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.item.ItemType
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class ItemTypeArgumentType : ArgumentType<ItemType>("item_type") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<ItemType> {
        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.unfatalize(diagnostics) { readNamespacedIdentifier() }
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, start..end))

        if (identifier !is TokenSeqReader.ReadResult.Successful) {
            return NoValuedArgument(this, start..end, diagnostics)
        }

        for (entry in ItemType.entries) {
            if (identifier.value == entry.identifier) {
                return Argument(this, entry, start..end)
            }
        }

        diagnostics.add(reader.diagnostic(start..end, "不明なアイテムタイプです: ${identifier.value}"))
        return NoValuedArgument(this, start..end, diagnostics)
    }

    override fun complete(sourceSpan: SourceSpan?, string: String, reader: TokenSeqReader): List<CompletionItem> {
        return ItemType.IDENTIFIERS
            .filter { it.startsWith(string) }
            .map {
                CompletionItem(it).apply {
                    textEdit = sourceSpan?.run {
                        Either.forLeft(TextEdit(sourceSpan.toLsp4jRange(), it))
                    }
                    kind = CompletionItemKind.EnumMember
                }
            }
    }
}
