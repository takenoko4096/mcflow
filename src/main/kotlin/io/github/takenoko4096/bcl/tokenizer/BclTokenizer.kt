package io.github.takenoko4096.bcl.tokenizer

import io.github.takenoko4096.bcl.BclException

class BclTokenizer(script: String) {
    private val text: String = script
        .replace(KeyChar.WINDOWS_LINE_SEPARATOR_SEQUENCE, KeyChar.LINEBREAK.toString())
        .replace(KeyChar.MAC_LINE_SEPARATOR, KeyChar.LINEBREAK)

    private var line: Int = 1

    private var cursor: Int = 0

    private var lastLineStartCursor: Int = 0

    /**
     * カーソルが文字列外であれば`true`
     */
    private fun isOver(): Boolean {
        return cursor >= text.length
    }

    private fun exception(message: String, cause: Throwable? = null): BclException {
        return BclTokenizationException(message, text, cursor, getCurrentSourcePosition(), cause)
    }

    private fun peek(): Char {
        if (isOver()) {
            throw exception("コード外に到達しました")
        }

        return text[cursor]
    }

    private fun peek(offset: Int): Char? {
        return text.getOrNull(cursor + offset)
    }

    private fun next() {
        cursor++

        if (peek(0) == KeyChar.LINEBREAK) {
            line++
            lastLineStartCursor = cursor
        }
    }

    private fun test(string: String): Boolean {
        for ((index, char) in string.withIndex()) {
            if (peek(index) != char) {
                return false
            }
        }

        return true
    }

    private fun testThenAdvance(string: String): Boolean {
        for ((index, char) in string.withIndex()) {
            if (peek(index) != char) {
                return false
            }
        }

        for (i in 0..<string.length) {
            next()
        }

        return true
    }

    private fun skipWhitespace(): Boolean {
        if (isOver()) return false

        var currentChar = peek()
        var includeLinebreak = false

        while (currentChar in KeyChar.WHITESPACE_CHARS) {
            if (currentChar == KeyChar.LINEBREAK) {
                includeLinebreak = true
            }

            next()
            if (isOver()) break
            currentChar = peek()
        }

        return includeLinebreak
    }

    private fun getCurrentSourcePosition(): SourcePosition {
        return SourcePosition(line, cursor - lastLineStartCursor)
    }

    private fun tokenOf(type: TokenType, value: String, start: SourcePosition): Token {
        return Token(type, value, start..getCurrentSourcePosition())
    }

    private fun tokenOf(symbol: Symbol, start: SourcePosition): Token {
        return Token(TokenType.SYMBOL, symbol.value, start..getCurrentSourcePosition())
    }

    private fun digits(radix: Int): String {
        val sb = StringBuilder()

        val numbers = KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<radix)

        while (!isOver() && peek() in numbers) {
            sb.append(peek())
            next()
        }

        return sb.toString()
    }

    /**
     * 0b, 0o, 0x, s(signed), u(unsigned), b, s, i, l, f, d は邪魔なのでここで削除する
     */
    private fun number(): Token {
        val sb = StringBuilder()

        val start = getCurrentSourcePosition()

        val radix = run {
            if (peek() != KeyChar.NONDECIMAL_MARKER) return@run Radix.DECIMAL

            for (entry in Radix.entries) {
                if (peek(1) == null) return@run Radix.DECIMAL

                if (peek(1) == entry.char) {
                    // sb.append(peek())
                    next()
                    // sb.append(peek())
                    next()
                    return@run entry
                }
            }

            return@run Radix.DECIMAL
        }

        val intPart = digits(radix.value)
        sb.append(intPart)

        if (!isOver() && (peek() == KeyChar.DECIMAL_POINT || peek().lowercaseChar() == KeyChar.EXPONENTIAL) && radix.value == 10) {
            if (test(Symbol.RANGE.value)) {
                return tokenOf(radix.apply(NumberType.INT.default(true)), sb.toString(), start)
            }

            sb.append(peek())
            next()
            sb.append(digits(10))

            return if (peek() == NumberType.FLOAT.char) {
                // sb.append(peek())
                next()
                tokenOf(TokenType.FLOAT, sb.toString(), start)
            }
            else if (peek() == NumberType.DOUBLE.char) {
                // sb.append(peek())
                next()
                tokenOf(TokenType.DOUBLE, sb.toString(), start)
            }
            else {
                tokenOf(TokenType.DOUBLE, sb.toString(), start)
            }
        }

        val suffix1 = peek(0)
        val suffix2 = peek(1)

        var signed = true
        var type = NumberType.INT
        var isEndWithValidTypeSuffix = false

        for (numberType in NumberType.entries) {
            if (suffix2 == numberType.char) {
                type = numberType

                if (suffix1 == null) {
                    throw exception("NEVER HAPPENS")
                }

                if (!suffix1.isJavaIdentifierStart()) {
                    break
                }

                signed = when (suffix1) {
                    KeyChar.SIGNED -> true
                    KeyChar.UNSIGNED -> false
                    else -> {
                        throw exception("${KeyChar.SIGNED}/${KeyChar.UNSIGNED} が期待されましたが、無効な数値接尾辞が検出されました: '$suffix1'")
                    }
                }

                // sb.append(peek())
                next()
                isEndWithValidTypeSuffix = true
                break
            }
            else if (suffix1 == numberType.char) {
                type = numberType
                signed = true
                isEndWithValidTypeSuffix = true
                break
            }
        }

        val tokenType = try {
            val type = radix.apply(type.default(signed))

            if (isEndWithValidTypeSuffix) {
                // sb.append(peek())
                next()
            }

            type
        }
        catch (error: IllegalArgumentException) {
            throw exception("基数が ${radix.value} の数値リテラルに型接尾辞 '${type.char}' は付与できません", error)
        }

        return tokenOf(
            tokenType,
            sb.toString(),
            start
        )
    }

    /**
     * ' は削除
     */
    private fun char(): Token {
        val sb = StringBuilder()

        val start = getCurrentSourcePosition()

        if (peek() != KeyChar.CHAR_QUOTE) {
            throw exception("文字が ${KeyChar.CHAR_QUOTE} で開始されていません")
        }

        // sb.append(peek())
        next()

        val char = peek()
        sb.append(peek())
        next()

        if (peek() == KeyChar.CHAR_QUOTE) {
            // sb.append(peek())
            next()
        }
        else if (char == KeyChar.CHAR_QUOTE) {
            throw exception("空の文字は無効です")
        }
        else {
            throw exception("文字は ${KeyChar.CHAR_QUOTE} で閉じる必要があります")
        }

        return tokenOf(TokenType.CHAR, sb.toString(), start)
    }

    /**
     * " は削除
     */
    private fun string(): Token {
        val sb = StringBuilder()

        val start = getCurrentSourcePosition()

        if (peek() !in KeyChar.STRING_QUOTE_CHARS) {
            throw exception("文字列の初めにはクォートが必要ですが、'${peek()}' が見つかりました")
        }

        val quote = peek()
        // sb.append(quote)
        next()

        if (peek() == quote) {
            // sb.append(quote)
            next()
            return tokenOf(TokenType.STRING, sb.toString(), start)
        }

        while (true) {
            if (peek() == quote) {
                // クォートだったら解析終了
                // sb.append(peek())
                next()
                break
            }
            else if (peek() == KeyChar.ESCAPE) {
                next()
                // エスケープだったら次の文字を今回のループにおける文字とする
                var c = peek()

                if (c == 'n') {
                    // これは流石に改行
                    c = '\n'
                }
                else if (c == 'r') {
                    // これは流石にキャリッジリターン
                    c = '\r'
                }

                sb.append(c)
                next()
            }
            else {
                // それ以外なら普通に連結
                sb.append(peek())
                next()
            }

            if (isOver()) {
                throw exception("文字列の解析中にコードの末尾に到達しました: $sb")
            }
        }

        return tokenOf(TokenType.STRING, sb.toString(), start)
    }

    private fun skipComment() {
        if (isOver()) return

        if (testThenAdvance(KeyChar.LINE_COMMENT_SEQUENCE)) {
            while (!isOver()) {
                if (peek() == KeyChar.LINEBREAK) break
                next()
            }
        }
        else if (testThenAdvance(KeyChar.BLOCK_COMMENT_START_SEQUENCE)) {
            var count = 1
            while (!isOver()) {
                if (testThenAdvance(KeyChar.BLOCK_COMMENT_START_SEQUENCE)) {
                    count++
                }
                else if (testThenAdvance(KeyChar.BLOCK_COMMENT_END_SEQUENCE)) {
                    count--
                }
                else {
                    next()
                }

                if (count == 0) break
            }
        }
    }

    private fun word(): Token {
        val sb = StringBuilder()

        val start = getCurrentSourcePosition()

        var current = peek()

        if (current.isJavaIdentifierStart()) {
            sb.append(current)
            next()
        }
        else {
            throw exception("識別子として無効な文字です: '$current'")
        }

        while (!isOver()) {
            current = peek()

            if (current.isJavaIdentifierPart()) {
                sb.append(current)
                next()
            }
            else break
        }

        return tokenOf(TokenType.WORD, sb.toString(), start)
    }

    private fun token(previous: Token?): Token? {
        val noSeparator = previous?.type !in setOf(TokenType.WORD, TokenType.SYMBOL, null)

        val linebreak = skipWhitespace()

        if (linebreak) {
            return tokenOf(Symbol.NEWLINE, getCurrentSourcePosition())
        }

        skipComment()

        if (isOver()) {
            return null
        }

        if (peek() == KeyChar.CHAR_QUOTE) {
            if (noSeparator) {
                throw exception("区切りが必要です")
            }

            return char()
        }

        if (peek() in KeyChar.STRING_QUOTE_CHARS) {
            if (noSeparator) {
                throw exception("区切りが必要です")
            }

            return string()
        }

        if (peek() in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<Radix.DECIMAL.value)) {
            if (noSeparator) {
                throw exception("区切りが必要です")
            }

            return number()
        }

        for (symbol in Symbol.entries.sortedByDescending { it.value.length }) {
            val start = getCurrentSourcePosition()
            if (testThenAdvance(symbol.value)) {
                return tokenOf(symbol, start)
            }
        }

        /*if (noSeparator) {
            throw exception("区切りが必要です")
        }*/

        return word()
    }

    fun tokenize(): List<Token> {
        try {
            var token: Token? = null
            val list = mutableListOf<Token>()

            while (!isOver()) {
                token = token(token) ?: break
                list.add(token)
            }

            // list.add(tokenOf(Symbol.SEMICOLON))

            return list
        }
        finally {
            cursor = 0
            line = 1
            lastLineStartCursor = 0
        }
    }
}
