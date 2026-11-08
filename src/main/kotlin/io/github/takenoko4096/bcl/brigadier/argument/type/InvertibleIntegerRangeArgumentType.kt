package io.github.takenoko4096.bcl.brigadier.argument.type

import io.github.takenoko4096.bcl.brigadier.argument.Argument
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.argument.ArgumentParseContext
import io.github.takenoko4096.bcl.brigadier.argument.NoValuedArgument
import io.github.takenoko4096.bcl.server.BclSemanticToken
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
    override fun parse(context: ArgumentParseContext): ArgumentParseResult<InvertibleIntegerRange> {
        val reader = context.reader
        // val diagnostics = context.diagnostics
        val semanticTokens = context.semanticTokens

        val (inv, invSourceSpan) = if (reader.peek() `is` Symbol.LOGICAL_NOT) {
            val start = reader.getCurrentTokenStartPosition()
            reader.next()
            val end = reader.getPreviousTokenEndPosition()
            true to start..end
        }
        else {
            false to null
        }

        val range = reader.unfatalize(context.diagnostics) { readIntRange() }

        if (range.sourceSpan != null) {
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, range.sourceSpan))
        }

        return if (range is TokenSeqReader.ReadResult.Successful) {
            val wholeSourceSpan = (invSourceSpan?.start ?: range.nonnullSourceSpan.start)..range.nonnullSourceSpan.end
            Argument(this, InvertibleIntegerRange(inv, range.value), wholeSourceSpan)
        }
        else {
            NoValuedArgument(this, invSourceSpan, context.diagnostics)
        }
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
