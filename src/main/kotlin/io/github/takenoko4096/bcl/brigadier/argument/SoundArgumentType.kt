package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.NoValuedArgument
import io.github.takenoko4096.bcl.brigadier.value.misc.SOUNDS
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class SoundArgumentType : ArgumentType<String>("sound") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<String> {
        val string = reader.unfatalize(diagnostics) { readQuotableCommandString() }

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ENUM, string.sourceSpan))

        if (string !is TokenSeqReader.ReadResult.Successful) {
            return NoValuedArgument(this, string.sourceSpan, diagnostics)
        }

        if (string.value !in SOUNDS) {
            diagnostics.add(reader.diagnostic(string.sourceSpan, "サウンドの読み取りに失敗しました: 不明なサウンドです: '${string.value}'"))
            return NoValuedArgument(this, string.sourceSpan, diagnostics)
        }

        return Argument(this, string.value, string.sourceSpan)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return SOUNDS
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
