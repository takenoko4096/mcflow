package io.github.takenoko4096.bcl.complete

import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either
import kotlin.collections.contains

class ArgumentCompleter(private val context: BclCompletionContext) {
    private var cursor: Int = 0

    private val remaining: String
        get() = context.string.substring(cursor)

    fun isOver(): Boolean = cursor >= context.string.length

    fun peek(): Char {
        if (isOver()) {
            throw BclArgumentCompletionException()
        }

        return remaining[cursor]
    }

    fun next() {
        if (!isOver()) {
            cursor++
        }
    }

    fun completeChar(char: Char, kind: CompletionItemKind): Result {
        if (isOver()) {
            return ShouldComplete(listOf(CompletionItem(char.toString()).apply {
                this.kind = kind
                context.sourceSpan?.run {
                    this@apply.textEdit = Either.forLeft(TextEdit(toLsp4jRange(), char.toString()))
                }
            }))
        }

        if (peek() == char) {
            next()
            return ShouldContinue(char.toString())
        }
        else {
            return ShouldComplete(emptyList())
        }
    }

    fun readString(): String {
        if (isOver()) {
            return ""
        }

        val start = cursor

        val firstChar = remaining.getOrNull(start)
        val secondChar = remaining.getOrNull(start + 1)

        when (firstChar) {
            KeyChar.COLON -> {
                return ""
            }
            KeyChar.WILDCARD -> {
                return firstChar.toString()
            }
            in setOf(KeyChar.PLUS, KeyChar.MINUS) -> {
                if (secondChar == null || secondChar in KeyChar.COMMAND_WORD_TERMINATORS) {
                    next()
                    return ""
                }

                next()
            }
        }

        if (!isOver()) while (!isOver() && peek() !in KeyChar.COMMAND_WORD_TERMINATORS) {
            next()
        }

        return context.string.substring(start, cursor)
    }

    /*fun readQuotedString(): String {
        val sb = StringBuilder()

        val start = cursor

        if (peek() !in KeyChar.QUOTE_CHARS) {
            return invalid(start..start, "文字列の初めにはクォートが必要ですが、'${peek()}' が見つかりました")
        }

        val quote = peek()
        // sb.append(quote)
        next()

        if (isOver()) {
            return invalid(start..start, "文字列の解析に失敗しました: 文字列が $quote で閉じられていません")
        }

        if (peek() == quote) {
            // sb.append(quote)
            val end = getCurrentSourcePosition()
            next()
            return tokenOf(TokenType.DOUBLE_QUOTED, sb.toString(), start, end)
        }

        var end: SourcePosition

        while (true) {
            if (isOver()) {
                end = getSourcePositionAtBack()
                return invalid(start..end, "文字列の解析に失敗しました: 文字列が $quote で閉じられていません: $sb")
            }

            if (peek() == quote) {
                // クォートだったら解析終了
                // sb.append(peek())

                end = getCurrentSourcePosition()
                next()
                break
            }
            else if (peek() == KeyChar.LINEBREAK) {
                end = getSourcePositionAtBack()
                return invalid(start..end, "文字列の解析に失敗しました: 文字列が $quote で閉じられていません; 文字列内で改行するには '${KeyChar.ESCAPE}' を使用してください: $sb")
            }
            else if (peek() == KeyChar.ESCAPE) {
                next()
                // エスケープだったら次の文字を今回のループにおける文字とする
                var c = peek()

                // ただし改行お前はダメだ
                if (c == KeyChar.LINEBREAK) {
                    next()
                    continue
                }

                if (c in KeyChar.CONTROL_CHARS) {
                    c = KeyChar.CONTROL_CHARS[c]!!
                }

                sb.append(c)
                next()
            }
            else {
                // それ以外なら普通に連結
                sb.append(peek())
                next()
            }
        }

        if (quote == KeyChar.SPECIAL_STRING_QUOTE) {
            return tokenOf(TokenType.BACK_QUOTED, sb.toString(), start, end)
        }

        return tokenOf(TokenType.DOUBLE_QUOTED, sb.toString(), start, end)
    }*/

    fun completeUnquotedString(candidates: List<String>, kind: CompletionItemKind): Result {
        val input = readString()

        val items = candidates
            .filter { it.startsWith(input) }
            .map {
                CompletionItem(it).apply {
                    this.kind = kind
                    context.sourceSpan?.run {
                        this@apply.textEdit = Either.forLeft(TextEdit(toLsp4jRange(), it))
                    }
                }
            }

        if (isOver()) {
            return ShouldComplete(items)
        }

        // null: "ここでは"補完しないべき、の意 次の文字に進む
        return ShouldContinue(input)
    }

    fun completeQuotableString(candidates: List<String>, kind: CompletionItemKind): Result {

    }

    sealed class Result
    class ShouldContinue(val input: String) : Result()
    class ShouldComplete(val items: List<CompletionItem>): Result()
}
