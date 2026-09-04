package io.github.takenoko4096.bcl.tokenizer

class BclTokenizer(text: String) {
    private val text: String = text
        .replace(KeyChar.WINDOWS_LINE_SEPARATOR_SEQUENCE, KeyChar.LINEBREAK.toString())
        .replace(KeyChar.MAC_LINE_SEPARATOR, KeyChar.LINEBREAK)

    private var line: Int = 1

    private var cursor: Int = 0

    private var lastLineStartCursor: Int = 0

    private var id = 0

    /**
     * カーソルが文字列外であれば`true`
     */
    private fun isOver(): Boolean {
        return cursor >= text.length
    }

    private fun exception(start: SourcePosition, message: String, cause: Throwable? = null): BclTokenizationException {
        return BclTokenizationException(message, text, start..getCurrentSourcePosition(), cause)
    }

    private fun peek(): Char {
        if (isOver()) {
            throw exception(getCurrentSourcePosition(), "コード外に到達しました")
        }

        return text[cursor]
    }

    private fun peek(offset: Int): Char? {
        return text.getOrNull(cursor + offset)
    }

    private fun next() {
        val current = peek()
        cursor++

        if (current == KeyChar.LINEBREAK) {
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

    private fun skipWhitespace(): WhitespaceSkipResult {
        if (isOver()) return WhitespaceSkipResult(false, null)

        var currentChar = peek()
        var result = WhitespaceSkipResult(false, null)

        while (currentChar in KeyChar.WHITESPACE_CHARS) {
            if (!result.foundWhitespace) result = WhitespaceSkipResult(true, result.linebreak)
            if (result.linebreak == null && currentChar == KeyChar.LINEBREAK) {
                result = WhitespaceSkipResult(result.foundWhitespace, getCurrentSourcePosition())
            }

            next()
            if (isOver()) break
            currentChar = peek()
        }

        return result
    }

    private fun getCurrentSourcePosition(): SourcePosition {
        return SourcePosition(line, cursor - lastLineStartCursor, cursor)
    }

    private fun tokenOf(type: TokenType, value: String, start: SourcePosition, end: SourcePosition): Token {
        return Token(id++, type, value, start..end)
    }

    private fun tokenOf(symbol: Symbol, start: SourcePosition, end: SourcePosition): Token {
        return Token(id++, TokenType.SYMBOL, symbol.value, start..end)
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

                if (peek(1) == entry.char && peek(2) in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<entry.value)) {
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

        if (intPart.isEmpty()) {
            throw exception(start, "空の数値リテラルは無効です")
        }

        sb.append(intPart)

        // (0 == 小数点 or 指数指定 'e') and 10進 and (1 <: 10進)
        // 小数とか型付きのときとか
        if (!isOver()
            && (peek() == KeyChar.DECIMAL_POINT || peek().lowercaseChar() == KeyChar.EXPONENTIAL)
            && radix.value == 10
            && peek(1) in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<radix.value)
        ) {
            sb.append(peek())
            next()
            sb.append(digits(10))

            return if (peek() == NumberType.FLOAT.char) {
                // sb.append(peek())
                val end = getCurrentSourcePosition()
                next()
                tokenOf(TokenType.FLOAT, sb.toString(), start, end)
            }
            else if (peek() == NumberType.DOUBLE.char) {
                // sb.append(peek())
                val end = getCurrentSourcePosition()
                next()
                tokenOf(TokenType.DOUBLE, sb.toString(), start, end)
            }
            else {
                val end = getCurrentSourcePosition()
                tokenOf(TokenType.DOUBLE, sb.toString(), start, end)
            }
        }

        // TODO ちょっと汚い
        cursor--
        var end = getCurrentSourcePosition()
        cursor++

        val suffix1 = peek(0)
        val suffix2 = peek(1)

        var signed = true
        var type = NumberType.INT
        var isEndWithValidTypeSuffix = false

        for (numberType in NumberType.entries) {
            if (suffix1 in setOf(KeyChar.SIGNED, KeyChar.UNSIGNED) && suffix2 == numberType.char) {
                type = numberType

                suffix1!!

                if (!suffix1.isJavaIdentifierStart()) {
                    break
                }

                signed = when (suffix1) {
                    KeyChar.SIGNED -> true
                    KeyChar.UNSIGNED -> false
                    else -> {
                        throw exception(start, "${KeyChar.SIGNED}/${KeyChar.UNSIGNED} が期待されましたが、無効な数値接尾辞が検出されました: '$suffix1'")
                    }
                }

                // sb.append(peek())
                end = getCurrentSourcePosition()
                next()
                isEndWithValidTypeSuffix = true
                break
            }
            else if (suffix1?.lowercaseChar() == numberType.char) {
                if (suffix2?.isJavaIdentifierPart() == true) {
                    next()
                    throw exception(start, "無効な数値接尾辞です: $suffix2")
                }

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
                end = getCurrentSourcePosition()
                next()
            }

            type
        }
        catch (error: IllegalArgumentException) {
            throw exception(start, "基数が ${radix.value} の数値リテラルに型接尾辞 '${type.char}' は付与できません", error)
        }

        return tokenOf(
            tokenType,
            sb.toString(),
            start,
            end
        )
    }

    /**
     * " は削除
     */
    private fun string(): Token {
        val sb = StringBuilder()

        val start = getCurrentSourcePosition()

        if (peek() !in KeyChar.QUOTE_CHARS) {
            throw exception(start, "文字列の初めにはクォートが必要ですが、'${peek()}' が見つかりました")
        }

        val quote = peek()
        // sb.append(quote)
        next()

        if (peek() == quote) {
            if (quote == KeyChar.CHAR_QUOTE) {
                throw exception(start, "空の文字リテラルは存在しません")
            }

            // sb.append(quote)
            val end = getCurrentSourcePosition()
            next()
            return tokenOf(TokenType.DOUBLE_QUOTED, sb.toString(), start, end)
        }

        var end: SourcePosition

        while (true) {
            if (peek() == quote) {
                // クォートだったら解析終了
                // sb.append(peek())

                if (quote == KeyChar.CHAR_QUOTE && sb.length > 1) {
                    throw exception(start, "長さが2以上の文字リテラルは存在しません")
                }

                end = getCurrentSourcePosition()
                next()
                break
            }
            else if (peek() == KeyChar.ESCAPE) {
                next()
                // エスケープだったら次の文字を今回のループにおける文字とする
                var c = peek()

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

            if (isOver()) {
                throw exception(start, "文字列の解析中にコードの末尾に到達しました: $sb")
            }
        }

        if (quote == KeyChar.SPECIAL_STRING_QUOTE) {
            return tokenOf(TokenType.BACK_QUOTED, sb.toString(), start, end)
        }

        return tokenOf(TokenType.DOUBLE_QUOTED, sb.toString(), start, end)
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
        }
        else {
            next()
            throw exception(start, "識別子として無効な文字です: '$current'")
        }

        var end = getCurrentSourcePosition()
        next()

        while (!isOver()) {
            current = peek()

            if (current.isJavaIdentifierPart()) {
                sb.append(current)
                end = getCurrentSourcePosition()
                next()
            }
            else break
        }

        return tokenOf(TokenType.WORD, sb.toString(), start, end)
    }

    private fun coordinate(marker: Char, type: TokenType): Token {
        val start = getCurrentSourcePosition()

        if (peek() != marker) {
            next()
            throw exception(start, "座標入力は $marker で開始されることが期待されましたが、 ${peek()} が見つかりました")
        }

        next()

        if (peek() == KeyChar.MINUS) {
            val sb = StringBuilder()
            if (peek() == KeyChar.MINUS) {
                sb.append(KeyChar.MINUS)
                next()
            }

            sb.append(number().value)

            return tokenOf(type, sb.toString(), start, getCurrentSourcePosition())
        }
        else if (peek() in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<Radix.DECIMAL.value)) {
            return tokenOf(type, number().value, start, getCurrentSourcePosition())
        }
        else {
            return tokenOf(type, "0", start, getCurrentSourcePosition())
        }
    }

    private fun token(previous: Token?): Token? {
        val start = getCurrentSourcePosition()

        val result = skipWhitespace()

        val noSeparator = previous != null
            && previous.type != TokenType.SYMBOL
            && previous.type != TokenType.COORDINATE_LOCAL
            && previous.type != TokenType.COORDINATE_RELATIVE
            && !result.foundWhitespace

        if (result.linebreak != null) {
            return tokenOf(Symbol.NEWLINE, result.linebreak, result.linebreak)
        }

        skipComment()

        if (isOver()) {
            return null
        }

        if (peek() in KeyChar.QUOTE_CHARS) {
            if (noSeparator) {
                throw exception(start, "区切りが必要です")
            }

            return string()
        }

        if (peek() in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<Radix.DECIMAL.value)) {
            if (noSeparator) {
                throw exception(start, "区切りが必要です")
            }

            return number()
        }

        if (peek() == KeyChar.COORDINATE_RELATIVE) {
            return coordinate(KeyChar.COORDINATE_RELATIVE, TokenType.COORDINATE_RELATIVE)
        }

        if (peek() == KeyChar.COORDINATE_LOCAL) {
            return coordinate(KeyChar.COORDINATE_LOCAL, TokenType.COORDINATE_LOCAL)
        }

        a@for (symbol in Symbol.entries.sortedByDescending { it.value.length }) {
            val start = getCurrentSourcePosition()

            for ((index, char) in symbol.value.withIndex()) {
                if (peek(index) != char) {
                    continue@a
                }
            }

            var end = getCurrentSourcePosition()
            for (i in 0..<symbol.value.length) {
                end = getCurrentSourcePosition()
                next()
            }

            return tokenOf(symbol, start, end)
        }

        if (noSeparator) {
            throw exception(start, "トークン $previous の直後には区切りが必要です")
        }

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
