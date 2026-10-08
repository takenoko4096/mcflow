package io.github.takenoko4096.bcl.completer

import io.github.takenoko4096.bcl.tokenizer.KeyChar
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

    fun completeChar(char: Char, kind: CompletionItemKind): ValueCompletionResult {
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

    open class QuotableString(val value: String)

    class QuotedString(
        value: String,
        val closed: Boolean
    ) : QuotableString(value)

    fun readQuotedString(): QuotedString {
        if (isOver()) {
            return QuotedString("", closed = false)
        }

        val sb = StringBuilder()

        val quote = peek()

        if (quote != KeyChar.STRING_QUOTE) {
            return QuotedString("", closed = false)
        }

        next()

        while (true) {
            if (isOver()) {
                return QuotedString(sb.toString(), closed = false)
            }

            if (peek() == quote) {
                next()
                return QuotedString(sb.toString(), closed = true)
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
    }

    fun readQuotableString(): QuotableString {
        if (isOver()) {
            return QuotableString("")
        }

        if (peek() == KeyChar.STRING_QUOTE) {
            return readQuotedString()
        }

        return QuotableString(readString())
    }

    fun completeUnquotedString(candidates: List<String>, kind: CompletionItemKind): ValueCompletionResult {
        val input = readString()

        val items = candidates
            .filter { it.startsWith(input) }
            .map {
                CompletionItem(it).apply {
                    this.kind = kind
                    // 補完時は常に context.sourceSpan を使うべき
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

    fun completeQuotedString(candidates: List<String>, kind: CompletionItemKind): ValueCompletionResult {
        val input = readQuotedString()

        val items = candidates
            .filter { it.startsWith(input.value) }
            .map {
                CompletionItem("\"$it\"").apply {
                    this.kind = kind
                    // 補完時は常に context.sourceSpan を使うべき
                    context.sourceSpan?.run {
                        this@apply.textEdit = Either.forLeft(TextEdit(toLsp4jRange(), it))
                    }
                }
            }

        return if (input.closed) {
            ShouldContinue(input.value)
        }
        else {
            ShouldComplete(items)
        }
    }

    fun completeQuotableString(candidates: List<String>, kind: CompletionItemKind): ValueCompletionResult {
        val input = readQuotableString()

        val items = candidates
            .filter { it.startsWith(input.value) }

        return if (input is QuotedString) {
            if (input.closed) {
                ShouldContinue(input.value)
            }
            else {
                ShouldComplete(items.map {
                    CompletionItem("\"$it\"").apply {
                        this.kind = kind
                        // 補完時は常に context.sourceSpan を使うべき
                        context.sourceSpan?.run {
                            this@apply.textEdit = Either.forLeft(TextEdit(toLsp4jRange(), it))
                        }
                    }
                })
            }
        }
        else {
            if (isOver()) {
                ShouldComplete(items.map {
                    CompletionItem(it).apply {
                        this.kind = kind
                        // 補完時は常に context.sourceSpan を使うべき
                        context.sourceSpan?.run {
                            this@apply.textEdit = Either.forLeft(TextEdit(toLsp4jRange(), it))
                        }
                    }
                })
            }
            else ShouldContinue(input.value)
        }
    }

    sealed class ValueCompletionResult
    class ShouldContinue(val input: String) : ValueCompletionResult()
    class ShouldComplete(val items: List<CompletionItem>): ValueCompletionResult()
}
