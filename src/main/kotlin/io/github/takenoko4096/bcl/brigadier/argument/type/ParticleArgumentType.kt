package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.misc.PARTICLES
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class ParticleArgumentType : IdentifierArgumentType("particle", defaultNamespace = NamespacedIdentifier.DEFAULT_NAMESPACE) {
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<NamespacedIdentifier> {
        val reader = context.reader
        val diagnostics = context.diagnostics
        // val semanticTokens = context.semanticTokens

        val result = super.parse(context)
        if (result is Argument) {
            if (result.value.toString() in PARTICLES) {
                return result
            }
            else if (context.sender.resourceUnvanillaReport == null) {
                return result
            }
            else {
                diagnostics.add(reader.diagnostic(
                    result.sourceSpan,
                    "パーティクル名の解析に失敗しました: 不明なパーティクルです: '${result.value}'",
                    context.sender.resourceUnvanillaReport!!
                ))
                return NoValuedArgument(this, result.sourceSpan, diagnostics)
            }
        }
        else {
            return result
        }
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return PARTICLES
            .map(NamespacedIdentifier::parse)
            .filter { it!!.toString().startsWith(context.string) || it.value.startsWith(context.string) }
            .map {
                CompletionItem(it.toString()).apply {
                    textEdit = context.sourceSpan?.run {
                        Either.forLeft(TextEdit(toLsp4jRange(), it.toString()))
                    }
                    kind = CompletionItemKind.EnumMember
                }
            }
    }
}
