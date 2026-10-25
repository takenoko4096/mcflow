package io.github.takenoko4096.bcl.completer

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.json.schema.CommandJsonSchema
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
import kotlin.text.startsWith

class ArgumentCompleter(private val context: BclCompletionContext) {
    private var cursor: Int = 0

    private var line: Int = 0

    private var lastLineStartCursor: Int = 0

    private val remaining: String
        get() = context.string.substring(this@ArgumentCompleter.cursor)

    fun isOver(): Boolean = this@ArgumentCompleter.cursor >= context.string.length

    fun copy(): ArgumentCompleter {
        val cpy = ArgumentCompleter(context)
        cpy.cursor = this@ArgumentCompleter.cursor
        cpy.lastLineStartCursor = lastLineStartCursor
        cpy.line = line
        return cpy
    }

    fun peek(): Char {
        if (isOver()) {
            throw BclArgumentCompletionException()
        }

        return context.string[this@ArgumentCompleter.cursor]
    }

    fun next() {
        val current = peek()
        this@ArgumentCompleter.cursor++

        if (current == KeyChar.LINEBREAK) {
            line++
            lastLineStartCursor = this@ArgumentCompleter.cursor
        }
    }

    fun testThenAdvance(target: String): Boolean {
        if (isOver()) {
            return false
        }

        val remaining = context.string.substring(cursor)

        if (remaining.startsWith(target)) {
            for (char in target) {
                next()
            }
            return true
        }

        return false
    }

    private fun getCurrentSourcePosition(): SourcePosition {
        return SourcePosition(
            line + context.offset.line,
            (this@ArgumentCompleter.cursor - lastLineStartCursor) + context.offset.column,
            this@ArgumentCompleter.cursor + context.offset.index
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
        val cpy = copy()
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
        else if (input == null) {
            if (optional && items.isEmpty()) {
                this@ArgumentCompleter.cursor = cpy.cursor
                lastLineStartCursor = cpy.lastLineStartCursor
                line = cpy.line
                ShouldContinue("")
            }
            else ShouldComplete(items)
        }
        else {
            if (optional) {
                this@ArgumentCompleter.cursor = cpy.cursor
                lastLineStartCursor = cpy.lastLineStartCursor
                line = cpy.line
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

    data class NumberString(
        val input: String,
        val complete: Boolean
    )

    fun readInteger(): NumberString {
        if (isOver()) {
            return NumberString(input = "", complete = false)
        }

        val sb = StringBuilder()

        if (peek() == KeyChar.MINUS) {
            sb.append(KeyChar.MINUS)
            next()
        }

        val intPart = digits()

        if (intPart.isEmpty()) {
            return NumberString(input = sb.toString(), complete = false)
        }

        sb.append(intPart)

        return NumberString(input = sb.toString(), complete = true)
    }

    fun readIntegerRange(): String {
        if (isOver()) {
            return ""
        }

        val sb = StringBuilder()

        if (testThenAdvance("..")) {
            sb.append("..")
            skipWhitespaceAndComments()
            sb.append(readInteger().input)
            return sb.toString()
        }

        val min = readInteger()
        sb.append(min.input)
        if (!min.complete) {
            return sb.toString()
        }

        skipWhitespaceAndComments()

        if (!testThenAdvance("..")) {
            return sb.toString()
        }
        sb.append("..")

        skipWhitespaceAndComments()

        if (isOver()) {
            return sb.toString()
        }

        if (peek() == KeyChar.MINUS || peek() in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<Radix.DECIMAL.value)) {
            val max = readInteger()
            sb.append(max)
            return sb.toString()
        }
        else {
            return sb.toString()
        }
    }

    fun readNumber(): NumberString {
        if (isOver()) {
            return NumberString(input = "", complete = false)
        }

        val sb = StringBuilder()

        if (peek() == KeyChar.MINUS) {
            sb.append(KeyChar.MINUS)
            next()
        }

        val intPart = digits()

        if (intPart.isEmpty()) {
            return NumberString(input =  sb.toString(), complete = false)
        }

        sb.append(intPart)

        if (isOver()) {
            return NumberString(input = sb.toString(), complete = true)
        }
        else if (peek() == KeyChar.DECIMAL_POINT) {
            sb.append(peek())
            next()
            val decimalPart = digits()

            if (decimalPart.isEmpty()) {
                return NumberString(input = sb.toString(), complete = false)
            }

            sb.append(decimalPart)

            return NumberString(input = sb.toString(), complete = true)
        }
        else {
            return NumberString(input = sb.toString(), complete = true)
        }
    }

    fun readComponent(): String {
        if (isOver()) {
            return ""
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
            sb.append(readNumber().input)
            sb.toString()
        }
        else {
            sb.toString()
        }
    }

    fun completeComponent(components: List<String>, kind: CompletionItemKind): ValueCompletionResult {
        val span = getRemainingSourceSpanOrNull()
        val input = readComponent()

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

    fun completeIntegerRange(ranges: List<String>, kind: CompletionItemKind): ValueCompletionResult {
        val span = getRemainingSourceSpanOrNull()
        val input = readIntegerRange()

        val items = ranges
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
        val x = readComponent().ifEmpty { null }
        val y = readComponent().ifEmpty { null }
        val z = readComponent().ifEmpty { null }
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

        val start = this@ArgumentCompleter.cursor

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

        return context.string.substring(start, this@ArgumentCompleter.cursor)
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

    fun completeInteger(candidates: List<Int>, kind: CompletionItemKind): ValueCompletionResult {
        val span = getRemainingSourceSpanOrNull()
        val input = readInteger().input

        val items = candidates
            .filter { it.toString().startsWith(input) }
            .map {
                CompletionItem(it.toString()).apply {
                    this.kind = kind
                    span?.run {
                        this@apply.textEdit = Either.forLeft(TextEdit(toLsp4jRange(), it.toString()))
                    }
                }
            }

        if (input.toIntOrNull() in candidates) {
            return ShouldContinue(input)
        }

        if (isOver()) {
            return ShouldComplete(items)
        }

        return ShouldContinue(input)
    }

    fun completeUnquotedString(candidates: List<String>, kind: CompletionItemKind, prefixable: Set<Char> = emptySet()): ValueCompletionResult {
        val span = getRemainingSourceSpanOrNull()
        val input = readString(prefixable)

        val items = candidates
            .filter {
                if (it.startsWith(input)) return@filter true
                val identifier = NamespacedIdentifier.parse(it) ?: return@filter false
                return@filter identifier.value.startsWith(input)
            }
            .map {
                CompletionItem(it).apply {
                    this.kind = kind
                    span?.run {
                        this@apply.textEdit = Either.forLeft(TextEdit(toLsp4jRange(), it))
                    }
                }
            }

        if (input in candidates) {
            return ShouldContinue(input)
        }

        if (isOver()) {
            return ShouldComplete(items)
        }

        return ShouldContinue(input)
    }

    fun completeQuotedString(candidates: List<String>, kind: CompletionItemKind): ValueCompletionResult {
        val span = getRemainingSourceSpanOrNull()
        val input = readQuotedString()

        val string = if (input.opened) input.value else readString()

        val items = candidates
            .filter { it.startsWith(string) }
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

    fun completeJsonNumber(): CommandJsonSchema.JsonCompletionResult {
        val start = this@ArgumentCompleter.cursor

        /*
         * -
         */
        if (!isOver() && peek() == KeyChar.MINUS) {
            next()

            if (isOver()) {
                return CommandJsonSchema.JsonCompletionResult.Complete(emptyList())
            }
        }

        /*
         * integer part
         *
         * JSONでは整数部分は
         *
         * 0
         * または
         * [1-9][0-9]*
         *
         * なので、例えば 01 は厳密には不正。
         */
        if (isOver() || peek() !in '0'..'9') {
            this@ArgumentCompleter.cursor = start
            return CommandJsonSchema.JsonCompletionResult.Invalid
        }

        if (peek() == '0') {
            next()

            /*
             * 0の直後に数字が来たら不正。
             */
            if (!isOver() && peek() in '0'..'9') {
                this@ArgumentCompleter.cursor = start
                return CommandJsonSchema.JsonCompletionResult.Invalid
            }
        } else {
            while (!isOver() && peek() in '0'..'9') {
                next()
            }
        }

        /*
         * fraction
         */
        if (!isOver() && peek() == KeyChar.DECIMAL_POINT) {
            next()

            /*
             * "1." は入力途中。
             */
            if (isOver()) {
                return CommandJsonSchema.JsonCompletionResult.Complete(emptyList())
            }

            /*
             * 小数点の直後には必ず数字が必要。
             */
            if (peek() !in '0'..'9') {
                this@ArgumentCompleter.cursor = start
                return CommandJsonSchema.JsonCompletionResult.Invalid
            }

            while (!isOver() && peek() in '0'..'9') {
                next()
            }
        }

        /*
         * exponent
         *
         * e / E
         */
        if (!isOver() && peek() in setOf('e', 'E')) {
            next()

            /*
             * "1e" は入力途中。
             */
            if (isOver()) {
                return CommandJsonSchema.JsonCompletionResult.Complete(emptyList())
            }

            /*
             * exponentの符号。
             */
            if (peek() == '+' || peek() == '-') {
                next()

                /*
                 * "1e+" は入力途中。
                 */
                if (isOver()) {
                    return CommandJsonSchema.JsonCompletionResult.Complete(emptyList())
                }
            }

            /*
             * exponentには必ず数字が必要。
             */
            if (peek() !in '0'..'9') {
                this@ArgumentCompleter.cursor = start
                return CommandJsonSchema.JsonCompletionResult.Invalid
            }

            while (!isOver() && peek() in '0'..'9') {
                next()
            }
        }

        return CommandJsonSchema.JsonCompletionResult.Continue
    }

    data class Checkpoint(
        val cursor: Int,
        val line: Int,
        val lastLineStartCursor: Int
    )

    fun checkpoint(): Checkpoint {
        return Checkpoint(
            cursor = cursor,
            line = line,
            lastLineStartCursor = lastLineStartCursor
        )
    }

    fun restore(checkpoint: Checkpoint) {
        cursor = checkpoint.cursor
        line = checkpoint.line
        lastLineStartCursor = checkpoint.lastLineStartCursor
    }

    fun skipWhitespaceAndComments() {
        while (!isOver()) {
            // whitespace
            if (peek() in KeyChar.WHITESPACE_CHARS) {
                next()
                continue
            }

            // block comment
            if (context.string.startsWith(KeyChar.BLOCK_COMMENT_START_SEQUENCE, this@ArgumentCompleter.cursor)) {
                repeat(KeyChar.BLOCK_COMMENT_START_SEQUENCE.length) {
                    next()
                }

                var count = 1

                while (!isOver() && count > 0) {
                    when {
                        context.string.startsWith(KeyChar.BLOCK_COMMENT_START_SEQUENCE, this@ArgumentCompleter.cursor) -> {
                            repeat(KeyChar.BLOCK_COMMENT_START_SEQUENCE.length) {
                                next()
                            }
                            count++
                        }

                        context.string.startsWith(KeyChar.BLOCK_COMMENT_END_SEQUENCE, this@ArgumentCompleter.cursor) -> {
                            repeat(KeyChar.BLOCK_COMMENT_END_SEQUENCE.length) {
                                next()
                            }
                            count--
                        }

                        else -> {
                            next()
                        }
                    }
                }

                continue
            }

            // line comment
            if (context.string.startsWith(KeyChar.LINE_COMMENT_SEQUENCE, this@ArgumentCompleter.cursor)) {
                repeat(KeyChar.LINE_COMMENT_SEQUENCE.length) {
                    next()
                }

                while (!isOver() && peek() != KeyChar.LINEBREAK) {
                    next()
                }

                continue
            }

            break
        }
    }

    fun combine(vararg completers: ArgumentCompleter.() -> ValueCompletionResult): ValueCompletionResult {
        if (completers.isEmpty()) {
            return ShouldComplete(emptyList())
        }

        val start = checkpoint()
        val candidates = mutableListOf<Candidate>()

        for (completer in completers) {
            restore(start)
            val result = completer(this)
            if (result is ShouldComplete && !result.items.isEmpty()) {
                candidates += Candidate(result = result, cursor = checkpoint())
            }
        }

        if (candidates.isEmpty()) {
            restore(start)
            return ShouldComplete(emptyList())
        }

        val maxCursorCandidate = candidates.maxBy { it.cursor.cursor }
        val bestCandidates = candidates.filter { it.cursor.cursor == maxCursorCandidate.cursor.cursor }

        val items = bestCandidates
            .flatMap { candidate ->
                when (candidate.result) {
                    is ShouldComplete -> candidate.result.items
                    is ShouldContinue -> emptyList()
                }
            }
            .distinctBy { it.label }

        restore(maxCursorCandidate.cursor)

        System.err.println("rem:"+remaining)

        return when {
            items.isNotEmpty() -> ShouldComplete(items)
            bestCandidates.all { it.result is ShouldContinue } -> ShouldContinue((maxCursorCandidate.result as ShouldContinue).input)
            else -> ShouldComplete(emptyList())
        }
    }

    sealed class ValueCompletionResult
    class ShouldContinue(val input: String) : ValueCompletionResult() {
        override fun toString() = "ShouldContinue(\"$input\")"
    }
    class ShouldComplete(val items: List<CompletionItem>): ValueCompletionResult() {
        override fun toString() = "ShouldComplete(${items.joinToString(", ") { it.label }})"
    }

    data class Candidate(val result: ValueCompletionResult, val cursor: Checkpoint)
}
