package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class StringEnumArgumentType(name: String, private val entries: Array<String>) : ArgumentType<String>(name) {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<String> {
        val start = reader.getCurrentTokenStartPosition()
        val string = reader.readQuotableCommandString()
        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ENUM, start..end))

        for (entry in entries) {
            if (entry == string) {
                return Argument(this, entry, start..end)
            }
        }

        diagnostics.add(reader.diagnostic(start..end, "不明なエントリです: '$string'"))

        return NoValuedArgument(this, start..end, diagnostics)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return entries
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
        fun of(name: String, entries: Array<String>): StringEnumArgumentType {
            return StringEnumArgumentType(name, entries)
        }
    }
}
