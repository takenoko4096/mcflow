package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
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
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<String> {
        val reader = context.reader
        val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

        val string = reader.unfatalize(diagnostics) { readQuotableCommandString() }

        if (string.sourceSpan != null) {
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.ENUM, string.sourceSpan))
        }

        if (string !is TokenSeqReader.ReadResult.Successful) {
            return NoValuedArgument(this, string.sourceSpan, diagnostics)
        }

        if (string.value !in SOUNDS && context.sender.resourceUnvanillaReport != null) {
            diagnostics.add(reader.diagnostic(
                string.nonnullSourceSpan,
                "サウンドの読み取りに失敗しました: 不明なサウンドです: '${string.value}'",
                context.sender.resourceUnvanillaReport!!
            ))
            return NoValuedArgument(this, string.sourceSpan, diagnostics)
        }

        return Argument(this, string.value, string.nonnullSourceSpan)
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
