package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.BclSeverity
import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.item.ItemType
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.value.item.VanillaItemType
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

        val result = reader.unfatalize(diagnostics) { readNamespacedIdentifier() }

        if (result.sourceSpan != null) {
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, result.sourceSpan))
        }

        if (result !is TokenSeqReader.ReadResult.Successful) {
            return NoValuedArgument(this, result.sourceSpan, diagnostics)
        }

        val identifier = result.value

        val itemType = VanillaItemType.get(identifier)
            ?.toItemType()
            ?: run {
                // unvanilla
                if (context.sender.behaviorUnvanillaReport != null) {
                    diagnostics.add(reader.diagnostic(
                        result.nonnullSourceSpan, "不明なアイテムタイプです: ${result.value}",
                        context.sender.behaviorUnvanillaReport!!
                    ))
                }

                if (context.sender.behaviorUnvanillaReport == BclSeverity.ERROR) {
                    return NoValuedArgument(this, result.nonnullSourceSpan, diagnostics)
                }

                ItemType(identifier, isVanilla = false)
            }

        return Argument(this, itemType, result.nonnullSourceSpan)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return VanillaItemType.IDENTIFIERS
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
