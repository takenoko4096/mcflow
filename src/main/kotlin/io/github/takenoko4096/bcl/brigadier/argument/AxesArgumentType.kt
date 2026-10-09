package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.location.Axis
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class AxesArgumentType : ArgumentType<Set<Axis>>("axes") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<Set<Axis>> {
        val start = reader.getCurrentTokenStartPosition()
        val string = reader.readCommandString()
        val end = reader.getPreviousTokenEndPosition()
        val sourceSpan = start..end

        val set = string.toCharArray()
            .map { char ->
                Axis.entries.find { it.getString() == char.toString() }
                    ?: run {
                        diagnostics.add(reader.diagnostic(sourceSpan, "軸の読み取りに失敗しました: 軸名 '$char' は無効です"))
                        null
                    }
            }
            .filterNotNull()

        val axes = mutableSetOf<Axis>()
        for (element in set) {
            if (element in axes) {
                diagnostics.add(reader.diagnostic(sourceSpan, "軸の読み取りに失敗しました: 軸 '$element' が重複しています"))
            }

            axes.add(element)
        }

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, sourceSpan))

        if (diagnostics.isNotEmpty()) {
            return NoValuedArgument(this, sourceSpan, diagnostics)
        }

        return Argument(this, axes, sourceSpan)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return listOf("x", "y", "z", "xy", "xz", "yz", "xyz")
            .filter { it.startsWith(context.string) }
            .map {
                CompletionItem(it).apply {
                    textEdit = context.sourceSpan?.run {
                        Either.forLeft(TextEdit(
                            toLsp4jRange(),
                            it
                        ))
                    }
                    kind = CompletionItemKind.EnumMember
                }
            }
    }
}
