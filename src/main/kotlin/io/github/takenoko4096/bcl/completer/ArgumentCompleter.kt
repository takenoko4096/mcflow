package io.github.takenoko4096.bcl.completer

import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.Radix
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.jsonrpc.messages.Either
import kotlin.collections.contains
import kotlin.text.plus

class ArgumentCompleter(private val context: BclCompletionContext) {
    private var cursor: Int = 0

    private var line: Int = 0

    private var lastLineStartCursor: Int = 0

    private val remaining: String
        get() = context.string.substring(cursor)

    fun isOver(): Boolean = cursor >= context.string.length

    fun peek(): Char {
        if (isOver()) {
            throw BclArgumentCompletionException()
        }

        return context.string[cursor]
    }

    fun next() {
        val current = peek()
        cursor++

        if (current == KeyChar.LINEBREAK) {
            line++
            lastLineStartCursor = cursor
        }
    }

    private fun getCurrentSourcePosition(): SourcePosition {
        return SourcePosition(
            line + context.offset.line,
            (cursor - lastLineStartCursor) + context.offset.column,
            cursor + context.offset.index
        )
    }

    private fun getRemainingSourceSpanOrNull(): SourceSpan? {
        return context.sourceSpan?.run {
            val start = getCurrentSourcePosition()
            if (end.index < start.index) null else start..end
        }
    }

    fun readChar(): Char? {
        if (isOver()) {
            return null
        }

        val char = peek()
        next()
        return char
    }

    fun completeChar(chars: List<Char>, kind: CompletionItemKind, optional: Boolean = false): ValueCompletionResult {
        val span = getRemainingSourceSpanOrNull()
        val c = cursor
        val input = readChar()

        val items = chars.map {
            CompletionItem(it.toString()).apply {
                this.kind = kind
                span?.run {
                    this@apply.textEdit = Either.forLeft(TextEdit(toLsp4jRange(), it.toString()))
                }
            }
        }

        return if (input in chars) {
            ShouldContinue(input.toString())
        }
        else if (isOver()) {
            if (optional && items.isEmpty()) {
                cursor = c
                ShouldContinue("")
            }
            else ShouldComplete(items)
        }
        else {
            if (optional) {
                cursor = c
                ShouldContinue("")
            }
            else ShouldComplete(emptyList())
        }
    }

    private fun digits(): String {
        val sb = StringBuilder()

        val numbers = KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<Radix.DECIMAL.value)

        while (!isOver() && peek() in numbers) {
            sb.append(peek())
            next()
        }

        return sb.toString()
    }

    fun readNumber(): String? {
        if (isOver()) {
            return null
        }

        val sb = StringBuilder()

        if (peek() == KeyChar.MINUS) {
            sb.append(KeyChar.MINUS)
            next()
        }

        val intPart = digits()

        if (intPart.isEmpty()) {
            return sb.toString()
        }

        sb.append(intPart)

        if (isOver()) {
            return sb.toString()
        }
        else if (peek() == KeyChar.DECIMAL_POINT) {
            sb.append(peek())
            next()
            sb.append(digits())

            return sb.toString()
        }
        else {
            return sb.toString()
        }
    }

    fun readComponent(): String? {
        if (isOver()) {
            return null
        }

        val sb = StringBuilder()

        if (peek() in charArrayOf('~', '^')) {
            sb.append(peek())
            next()
        }

        return if (isOver()) {
            sb.toString()
        }
        else if (peek() == KeyChar.MINUS || peek() in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<Radix.DECIMAL.value)) {
            sb.append(readNumber() ?: throw IllegalStateException("NEVER HAPPENS"))
            sb.toString()
        }
        else {
            sb.toString()
        }
    }

    fun completeComponent(components: List<String>, kind: CompletionItemKind): ValueCompletionResult {
        val span = getRemainingSourceSpanOrNull()
        val input = readComponent() ?: ""

        val items = components
            .filter { it.startsWith(input) }
            .map {
                CompletionItem(it).apply {
                    this.kind = kind
                    span?.run {
                        textEdit = Either.forLeft(TextEdit(
                            toLsp4jRange(),
                            it
                        ))
                    }
                }
            }

        if (isOver()) {
            return ShouldComplete(items)
        }

        return ShouldContinue(input)
    }

    fun completeCoordinate(components: List<String>, kind: CompletionItemKind): ValueCompletionResult {
        val span = getRemainingSourceSpanOrNull()
        val x = readComponent()
        val y = readComponent()
        val z = readComponent()
        val input = x?.run { x + (y?.run { ' ' + y + (z?.run { " $z" } ?: "") } ?: "") } ?: ""

        val items = components
            .filter { it.startsWith(input) }
            .map {
                CompletionItem(it).apply {
                    this.kind = kind
                    span?.run {
                        textEdit = Either.forLeft(TextEdit(
                            toLsp4jRange(),
                            it
                        ))
                    }
                }
            }

        if (isOver()) {
            return ShouldComplete(items)
        }

        return ShouldContinue(input)
    }

    fun readString(prefixable: Set<Char> = emptySet()): String {
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
            in prefixable -> next()
        }

        if (!isOver()) while (!isOver() && peek() !in KeyChar.COMMAND_WORD_TERMINATORS) {
            next()
        }

        return context.string.substring(start, cursor)
    }

    open class QuotableString(val value: String)

    class QuotedString(
        value: String,
        val opened: Boolean,
        val closed: Boolean
    ) : QuotableString(value)

    fun readQuotedString(): QuotedString {
        if (isOver()) {
            return QuotedString("", opened = false, closed = false)
        }

        val sb = StringBuilder()

        val quote = peek()

        if (quote != KeyChar.STRING_QUOTE) {
            return QuotedString("", opened = false, closed = false)
        }

        next()

        while (true) {
            if (isOver()) {
                return QuotedString(sb.toString(), opened = true, closed = false)
            }

            if (peek() == quote) {
                next()
                return QuotedString(sb.toString(), opened = true, closed = true)
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

    fun readQuotableString(prefixable: Set<Char> = emptySet()): QuotableString {
        if (isOver()) {
            return QuotableString("")
        }

        if (peek() == KeyChar.STRING_QUOTE) {
            return readQuotedString()
        }

        return QuotableString(readString(prefixable))
    }

    fun completeUnquotedString(candidates: List<String>, kind: CompletionItemKind, prefixable: Set<Char> = emptySet()): ValueCompletionResult {
        val span = getRemainingSourceSpanOrNull()
        val input = readString(prefixable)

        val items = candidates
            .filter { it.startsWith(input) }
            .map {
                CompletionItem(it).apply {
                    this.kind = kind
                    span?.run {
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
        val span = getRemainingSourceSpanOrNull()
        val input = readQuotedString()

        val items = candidates
            .filter { it.startsWith(input.value) }
            .map {
                CompletionItem("\"$it\"").apply {
                    this.kind = kind
                    span?.run {
                        var s = '"' + it
                        if (!input.opened) {
                            s += '"'
                        }
                        this@apply.textEdit = Either.forLeft(TextEdit(toLsp4jRange(), s))
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

    fun completeQuotableString(candidates: List<String>, kind: CompletionItemKind, prefixable: Set<Char> = emptySet()): ValueCompletionResult {
        val span = getRemainingSourceSpanOrNull()
        val input = readQuotableString(prefixable)

        val items = candidates
            .filter { it.startsWith(input.value) }
            .map {
                CompletionItem(it).apply {
                    this.kind = kind
                    span?.run {
                        if (input is QuotedString) {
                            var s = '"' + it
                            if (!input.opened) {
                                s += '"'
                            }
                            this@apply.textEdit = Either.forLeft(TextEdit(toLsp4jRange(), s))
                        }
                        else {
                            this@apply.textEdit = Either.forLeft(TextEdit(toLsp4jRange(), it))
                        }
                    }
                }
            }

        return if (input is QuotedString) {
            if (input.closed) {
                ShouldContinue(input.value)
            }
            else {
                ShouldComplete(items)
            }
        }
        else {
            if (isOver()) {
                ShouldComplete(items)
            }
            else {
                ShouldContinue(input.value)
            }
        }
    }

    sealed class ValueCompletionResult
    class ShouldContinue(val input: String) : ValueCompletionResult()
    class ShouldComplete(val items: List<CompletionItem>): ValueCompletionResult()
}
