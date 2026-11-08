package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.BclSeverity
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.block.BlockType
import io.github.takenoko4096.bcl.brigadier.value.block.VanillaBlockType
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class BlockTypeArgumentType : ArgumentType<BlockType>("block_type") {
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<BlockType> {
        val reader = context.reader
        val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

        val identifier = reader.unfatalize(diagnostics) { readNamespacedIdentifier() }

        if (identifier.sourceSpan != null) {
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, identifier.sourceSpan))
        }

        if (identifier !is TokenSeqReader.ReadResult.Successful) {
            return NoValuedArgument(this, identifier.sourceSpan, diagnostics)
        }

        for (entry in VanillaBlockType.entries) {
            if (identifier.value == entry.identifier) {
                return Argument(this, entry.toBlockType(), identifier.nonnullSourceSpan)
            }
        }

        if (context.sender.behaviorUnvanillaReport != null) {
            diagnostics.add(reader.diagnostic(
                identifier.nonnullSourceSpan,
                "不明なブロックタイプです: ${identifier.value}",
                context.sender.behaviorUnvanillaReport!!
            ))

            if (context.sender.behaviorUnvanillaReport == BclSeverity.ERROR) {
                return NoValuedArgument(this, identifier.nonnullSourceSpan, diagnostics)
            }

            return Argument(this, BlockType(identifier.value, setOf(), isVanilla = false), identifier.nonnullSourceSpan)
        }

        // unvanilla
        return Argument(this, BlockType(identifier.value, setOf(), isVanilla = false), identifier.nonnullSourceSpan)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return VanillaBlockType.IDENTIFIERS
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
