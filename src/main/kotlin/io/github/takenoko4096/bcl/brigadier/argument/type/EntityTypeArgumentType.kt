package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.BclSeverity
import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.entity.EntityType
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.value.entity.VanillaEntityType
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class EntityTypeArgumentType(private val summonableOnly: Boolean) : ArgumentType<EntityType>("entity_type") {
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<EntityType> {
        val reader = context.reader
        val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

        val start = reader.getCurrentTokenStartPosition()
        val identifier = reader.readNamespacedIdentifier()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, start..end))

        val entityType = VanillaEntityType.get(identifier)
            ?.toEntityType()
            ?: run {
                // unvanilla
                if (context.sender.behaviorUnvanillaReport != null) {
                    diagnostics.add(reader.diagnostic(
                        start..end, "不明なエンティティタイプです: ${identifier.value}",
                        context.sender.behaviorUnvanillaReport!!
                    ))

                    if (context.sender.behaviorUnvanillaReport == BclSeverity.ERROR) {
                        return NoValuedArgument(this, start..end, diagnostics)
                    }
                }

                EntityType(identifier, isSummonable = true, isVanilla = false)
            }

        if (summonableOnly && !entityType.isSummonable) {
            diagnostics.add(reader.diagnostic(
                start..end,
                "召喚不可能なエンティティタイプです: ${identifier.value}"
            ))
            return NoValuedArgument(this, start..end, diagnostics)
        }

        return Argument(this, entityType, start..end)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return (if (summonableOnly) VanillaEntityType.SUMMONABLE_IDENTIFIERS else VanillaEntityType.IDENTIFIERS)
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

    companion object {
        fun entityType() = EntityTypeArgumentType(summonableOnly = false)

        fun summonableOnly() = EntityTypeArgumentType(summonableOnly = true)
    }
}
