package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.brigadier.Argument
import io.github.takenoko4096.bcl.brigadier.ArgumentParseResult
import io.github.takenoko4096.bcl.brigadier.BclCompletionContext
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.brigadier.value.misc.ScoreOperator
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either
import kotlin.collections.filter

class ScoreOperatorArgumentType : ArgumentType<ScoreOperator>("score_operator") {
    override fun parse(reader: TokenSeqReader, diagnostics: MutableList<BclDiagnostic>, semanticTokens: MutableList<BclSemanticToken>): ArgumentParseResult<ScoreOperator> {
        val start = reader.getCurrentTokenStartPosition()

        for (operator in ScoreOperator.entries) {
            if (reader.peek() `is` operator.symbol) {
                reader.next()
                val end = reader.getPreviousTokenEndPosition()
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, start..end))

                return Argument(this, operator, start..end)
            }
        }

        reader.next()
        val end = reader.getPreviousTokenEndPosition()
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, start..end))
        throw reader.exception(start..end, "スコア演算子の解析に失敗しました: 無効なトークンです: ${reader.peekSafely(-1)}")
    }

    override fun complete(context: BclCompletionContext): List<CompletionItem> {
        return ScoreOperator.entries
            .filter { it.symbol.value.startsWith(context.string) }
            .map {
                CompletionItem(it.symbol.value).apply {
                    textEdit = context.sourceSpan?.run {
                        Either.forLeft(TextEdit(
                            toLsp4jRange(),
                            it.symbol.value
                        ))
                    }
                    kind = CompletionItemKind.Operator
                }
            }
    }
}
