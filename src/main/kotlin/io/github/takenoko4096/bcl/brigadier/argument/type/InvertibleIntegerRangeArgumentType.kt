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

        val start = reader.getCurrentTokenStartPosition()

        val inv = if (reader.peek() `is` Symbol.LOGICAL_NOT) {
            reader.next()
            true
        }
        else false

        val range = reader.unfatalize(context.diagnostics) { readIntRange() }

        if (range.nonnull) {
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, range.sourceSpan))
        }

        val wholeSourceSpan = start..range.sourceSpan.end

        return if (range is TokenSeqReader.ReadResult.Successful) {
            Argument(this, InvertibleIntegerRange(inv, range.value), wholeSourceSpan)
        }
        else {
            NoValuedArgument(this, wholeSourceSpan, context.diagnostics)
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
