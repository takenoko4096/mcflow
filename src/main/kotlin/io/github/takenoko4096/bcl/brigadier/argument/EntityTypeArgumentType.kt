package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.entity.EntityType
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class EntityTypeArgumentType(private val summonableOnly: Boolean) : ArgumentType<EntityType>("entity_type") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<EntityType> {
        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.unfatalize(diagnostics) { readNamespacedIdentifier() }
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, start..end))

        if (identifier is TokenSeqReader.ReadResult.Failure) {
            return NoValuedArgument(this, start..end, diagnostics)
        }

        identifier as TokenSeqReader.ReadResult.Successful

        val entityType = EntityType.get(identifier.value) ?: run {
            diagnostics.add(reader.diagnostic(start..end, "不明なエンティティタイプです: ${identifier.value}"))
            return NoValuedArgument(this, start..end, diagnostics)
        }

        if (summonableOnly && !entityType.summonable) {
            diagnostics.add(reader.diagnostic(start..end, "召喚不可能なエンティティタイプです: ${identifier.value}"))
            return NoValuedArgument(this, start..end, diagnostics)
        }

        return Argument(this, entityType, start..end)
    }

    override fun complete(sourceSpan: SourceSpan?, string: String, reader: TokenSeqReader, result: CommandLineParseResult): List<CompletionItem> {
        return EntityType.IDENTIFIERS
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

    companion object {
        fun entityType() = EntityTypeArgumentType(summonableOnly = false)

        fun summonableOnly() = EntityTypeArgumentType(summonableOnly = true)
    }
}
