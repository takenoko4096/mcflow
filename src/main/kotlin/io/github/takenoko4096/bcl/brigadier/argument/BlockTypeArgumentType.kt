package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.block.BlockType
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class BlockTypeArgumentType : ArgumentType<BlockType>("block_type") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<BlockType> {
        val identifier = reader.unfatalize(diagnostics) { readNamespacedIdentifier() }

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, identifier.sourceSpan))

        if (identifier !is TokenSeqReader.ReadResult.Successful) {
            return NoValuedArgument(this, identifier.sourceSpan, diagnostics)
        }

        for (entry in BlockType.entries) {
            if (identifier.value == entry.identifier) {
                return Argument(this, entry, identifier.sourceSpan)
            }
        }

        diagnostics.add(reader.diagnostic(identifier.sourceSpan, "不明なブロックタイプです: ${identifier.value}"))
        return NoValuedArgument(this, identifier.sourceSpan, diagnostics)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return BlockType.IDENTIFIERS
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
