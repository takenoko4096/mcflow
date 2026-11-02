package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.value.misc.InvertibleIntegerRange
import io.github.takenoko4096.bcl.completer.BclCompletionContext
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.Symbol
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either

class InvertibleIntegerRangeArgumentType : ArgumentType<InvertibleIntegerRange>("integer_range") {
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<InvertibleIntegerRange> {
        val reader = context.reader
        // val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

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
