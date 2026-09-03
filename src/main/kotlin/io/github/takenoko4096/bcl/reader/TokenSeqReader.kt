package io.github.takenoko4096.bcl.reader

import io.github.takenoko4096.bcl.parser.Keyword
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType

open class TokenSeqReader(
    protected val text: String,
    protected val tokens: List<Token>,
    private val exceptionConstructor: (message: String, text: String, span: SourceSpan, cause: Throwable?) -> BclTokenSeqReadException
) {
    var cursor: Int = 0

    fun clone(): TokenSeqReader {
        val cpy = TokenSeqReader(text, tokens, exceptionConstructor)
        cpy.cursor = cursor
        return cpy
    }

    fun isOver(): Boolean {
        return cursor >= tokens.size
    }

    fun peek(): Token {
        if (isOver()) {
            throw exception(getCurrentTokenStartPosition(), "コード外に到達しました")
        }

        return tokens[cursor]
    }

    fun peek(offset: Int): Token? {
        return tokens.getOrNull(cursor + offset)
    }

    fun peekSafely(offset: Int): Token {
        return if (cursor + offset < 0) {
            tokens.first()
        }
        else if (cursor + offset > tokens.lastIndex) {
            tokens.last()
        }
        else {
            tokens[cursor + offset]
        }
    }

    fun next() {
        cursor++
    }

    fun expect(symbol: Symbol) {
        val start = getCurrentTokenStartPosition()
        if (peek() `is` symbol) {
            next()
        }
        else {
            val v = peek().value
            next()
            throw exception(start, "予期しないトークンです: $v")
        }
    }

    fun isOverIgnoreNewLine(): Boolean {
        var i = 0
        var token: Token?
        do {
            token = peek(i) ?: return true
            i++
        }
        while (token `is` Symbol.NEWLINE)

        return false
    }

    fun peekIgnoreNewLine(): Token {
        val start = getCurrentTokenStartPosition()
        var i = 0
        var token: Token?
        do {
            token = peek(i) ?: run {
                for (j in 0..i) next()
                throw exception(start, "コード外に到達しました")
            }
            i++
        }
        while (token `is` Symbol.NEWLINE)

        return token
    }

    fun peekIgnoreNewLine(offset: Int): Token? {
        var i = 0
        var token: Token? = null
        var count = 0

        while (count <= offset) {
            do {
                token = peek(i) ?: return null
                i++
            }
            while (token `is` Symbol.NEWLINE)

            count++
        }

        return token
    }

    fun skipNewLine() {
        while (!isOver() && peek() `is` Symbol.NEWLINE) {
            next()
        }
    }

    fun nextIgnoreNewLine() {
        skipNewLine()
        next()
    }

    fun expectIgnoreNewLine(symbol: Symbol) {
        val start = getCurrentTokenStartPosition()
        if (peekIgnoreNewLine() `is` symbol) {
            nextIgnoreNewLine()
        }
        else {
            val v = peekIgnoreNewLine().value
            nextIgnoreNewLine()
            throw exception(start, "${symbol.value} が期待されましたが、予期しないトークンが見つかりました: $v")
        }
    }

    fun expectIgnoreNewLine(keyword: Keyword) {
        val start = getCurrentTokenStartPosition()
        if (isOverIgnoreNewLine()) {
            throw exception(start, "期待されたトークン ${keyword.value} に対してコード外への到達が返されました")
        }

        if (peekIgnoreNewLine() `is` keyword) {
            nextIgnoreNewLine()
        }
        else {
            val v = peekIgnoreNewLine().value
            nextIgnoreNewLine()
            throw exception(start, "期待されたトークン ${keyword.value} に対して予期しないトークンです: $v")
        }
    }

    fun read(vararg types: TokenType): String {
        if (isOver()) {
            throw exception(peekSafely(-1).sourceSpan, "$types のうちいずれかのトークンが期待されましたが、コード外に到達しました")
        }

        val start = getCurrentTokenStartPosition()
        val current = peek()
        if (current.type in types) {
            next()
            return current.value
        }
        else {
            next()
            throw exception(start, "$types のうちいずれかのトークンが期待されましたが、トークンの種類が一致しませんでした: ${current.type} ($current)")
        }
    }

    fun readIgnoreNewLine(vararg types: TokenType): String {
        if (isOverIgnoreNewLine()) {
            throw exception(peekSafely(-1).sourceSpan, "$types のうちいずれかのトークンが期待されましたが、コード外に到達しました")
        }

        val start = getCurrentTokenStartPosition()
        val current = peekIgnoreNewLine()
        if (current.type in types) {
            nextIgnoreNewLine()
            return current.value
        }
        else {
            nextIgnoreNewLine()
            throw exception(start, "$types のうちいずれかのトークンが期待されましたが、トークンの種類が一致しませんでした: ${current.type} ($current)")
        }
    }

    fun getCurrentTokenStartPosition(): SourcePosition {
        return peekSafely(0).sourceSpan.start
    }

    fun getPreviousTokenEndPosition(): SourcePosition {
        return peekSafely(-1).sourceSpan.end
    }

    fun readInteger(): Int {
        val start = getCurrentTokenStartPosition()

        val scale: Int
        val t = peek()
        if (t.type == TokenType.SYMBOL) {
            scale = if (t `is` Symbol.PLUS) {
                1
            }
            else if (t `is` Symbol.MINUS) {
                -1
            }
            else {
                next()
                throw exception(start, "整数の解析に失敗しました: 不明なトークンです: $t")
            }

            next()
        }
        else {
            scale = 1
        }

        val u = peek()

        val v = when (u.type) {
            TokenType.INT_RAD2 -> u.value.toInt(2) * scale
            TokenType.INT_RAD8 -> u.value.toInt(8) * scale
            TokenType.INT -> u.value.toInt() * scale
            TokenType.INT_RAD16 -> u.value.toInt(16) * scale
            TokenType.UINT_RAD2 -> u.value.toUInt(2).toInt() * scale
            TokenType.UINT_RAD8 -> u.value.toUInt(8).toInt() * scale
            TokenType.UINT -> u.value.toUInt().toInt() * scale
            TokenType.UINT_RAD16 -> u.value.toUInt(16).toInt() * scale
            else -> {
                next()
                throw exception(start, "整数の解析に失敗しました: 不明なトークンです: $u")
            }
        }

        next()

        return v
    }

    fun readQuotedString(): String {
        return read(TokenType.DOUBLE_QUOTED)
    }

    fun readWord(): String {
        return read(TokenType.WORD)
    }

    fun readUnquotableString(): String {
        return read(TokenType.DOUBLE_QUOTED, TokenType.WORD)
    }

    fun readBool(): Boolean {
        val start = getCurrentTokenStartPosition()
        val word = readWord()

        next()
        return when (word) {
            "true" -> true
            "false" -> false
            else -> {
                throw exception(start, "真偽値の解析に失敗しました: 不明なトークンです: $word")
            }
        }
    }

    fun readAllRemainingAsString(): String {
        return tokens.slice(cursor..<tokens.size).reduce { previous, current ->
            val spacer = " ".repeat(current.sourceSpan.start.index - previous.sourceSpan.end.index - 1)
            Token(-1, TokenType.WORD, previous.value + spacer + current.value, previous.sourceSpan.start..current.sourceSpan.end)
        }.also { cursor = tokens.size }.value
    }

    fun exception(sourceSpan: SourceSpan, message: String, cause: Throwable? = null): BclTokenSeqReadException {
        return exceptionConstructor(message, text, sourceSpan, cause)
    }

    fun exception(start: SourcePosition, message: String, cause: Throwable? = null): BclTokenSeqReadException {
        return exceptionConstructor(message, text, start..getPreviousTokenEndPosition(), cause)
    }

    fun getTokens2() = tokens
}
