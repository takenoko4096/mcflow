package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.misc.InvertibleIntegerRange
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.Symbol
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class InvertibleIntegerRangeArgumentType : ArgumentType<InvertibleIntegerRange>("integer_range") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<InvertibleIntegerRange> {
        val start = reader.getCurrentTokenStartPosition()

        val inv = if (reader.peek() `is` Symbol.LOGICAL_NOT) {
            reader.next()
            true
        }
        else false

        val start2 = reader.getCurrentTokenStartPosition()

        val range = reader.readIntRange()

        val end = reader.getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, start2..end))

        return Argument(this, InvertibleIntegerRange(inv, range), start..end)
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return listOf("-2147483648..2147483647")
            .filter { it.startsWith(context.string) }
            .map {
                CompletionItem(it).apply {
                    kind = CompletionItemKind.Value
                    context.sourceSpan?.run {
                        textEdit = Either.forLeft(TextEdit(
                            toLsp4jRange(),
                            it
                        ))
                    }
                }
            }
    }
}
