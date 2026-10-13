package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.misc.PARTICLES
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticToken
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class ParticleArgumentType : IdentifierArgumentType("particle", defaultNamespace = NamespacedIdentifier.DEFAULT_NAMESPACE) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<NamespacedIdentifier> {
        val result = super.parse(reader, diagnostics, semanticTokens)
        if (result is Argument) {
            if (result.value.toString() in PARTICLES) {
                return result
            }
            else {
                diagnostics.add(reader.diagnostic(result.sourceSpan, "パーティクル名の解析に失敗しました: 不明なパーティクルです: '${result.value}'"))
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
