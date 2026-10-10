package io.github.takenoko4096.bcl.tokenizer

class BclTokenizer(text: String, private val interpretComments: Boolean, private val commentRangeCollector: MutableList<SourceSpan> = mutableListOf()) {
    val sourceCode: String = text
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
        return cursor >= sourceCode.length
    }

    private fun invalid(sourceSpan: SourceSpan, type: TokenType, message: String): Token.Invalid {
        return Token.Invalid(id++, type, message, sourceSpan)
    }

    private fun exception(sourceSpan: SourceSpan, message: String, cause: Throwable? = null): BclTokenizationException {
        return BclTokenizationException(message, sourceCode, sourceSpan, cause)
    }

    private fun peek(): Char {
        if (isOver()) {
            throw exception(getSourcePositionAtBack()..getSourcePositionAtBack(), "入力が不足しています")
        }

        return sourceCode[cursor]
    }

    private fun peek(offset: Int): Char? {
        return sourceCode.getOrNull(cursor + offset)
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
        return SourcePosition(
            line,
            (cursor - lastLineStartCursor),
            cursor
        )
    }

    private fun getSourcePositionAtBack(): SourcePosition {
        cursor--
        val p = getCurrentSourcePosition()
        cursor++
        return p
    }

    private fun tokenOf(type: TokenType, value: String, start: SourcePosition, end: SourcePosition): Token {
        return Token(
            id++,
            type,
            value,
            start..end
        )
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
     * 単項演算子マイナスも含む
     */
    private fun number(): Token {
        val sb = StringBuilder()

        val start = getCurrentSourcePosition()

        if (peek() == KeyChar.MINUS) {
            sb.append(KeyChar.MINUS)
            next()
        }

        val radix = run {
            if (peek() != KeyChar.ZERO) return@run Radix.DECIMAL

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
            val end = getSourcePositionAtBack()
            return invalid(start..end, radix.apply(true), "数字を含まない数値リテラルは無効です")
        }

        sb.append(intPart)

        // ([0] == 小数点 or 指数指定 'e') and 10進 and ([1] <: 基数)
        if (!isOver()
            && (peek() == KeyChar.DECIMAL_POINT || peek().lowercaseChar() == KeyChar.EXPONENTIAL)
            && radix.value == 10
            && peek(1) in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<radix.value)
        ) {
            val isExp = peek().lowercaseChar() == KeyChar.EXPONENTIAL

            sb.append(peek())
            next()
            sb.append(digits(10))

            val end = getSourcePositionAtBack()

            val tokenType = if (isExp) {
                TokenType.FLOAT_WITH_EXPONENT
            }
            else {
                TokenType.FLOAT
            }

            return tokenOf(tokenType, sb.toString(), start, end)
        }

        var end = getSourcePositionAtBack()

        if (isOver()) {
            return tokenOf(radix.apply(true), sb.toString(), start, end)
        }

        val suffix = peek()
        var signed = true
        var integerPostfix: Char? = null
        if (suffix == KeyChar.UNSIGNED) {
            signed = false
            end = getCurrentSourcePosition()
            next()
        }
        else if (suffix in KeyChar.INTEGER_POSTFIXES) {
            end = getCurrentSourcePosition()
            next()
            integerPostfix = suffix
        }

        if (sb.startsWith(KeyChar.MINUS) && !signed) {
            end = getCurrentSourcePosition()
            next()
            return invalid(start..end, radix.apply(signed = false), "符号無し整数に負の符号が付いています")
        }

        if (integerPostfix != null) {
            val type = when (integerPostfix) {
                'd' -> TokenType.TIME_D
                's' -> TokenType.TIME_S
                't' -> TokenType.TIME_T
                'l' -> TokenType.LEVEL
                else -> throw IllegalStateException("NEVER HAPPENS")
            }

            if (radix != Radix.DECIMAL) {
                return invalid(start..end, type, "整数接尾辞は10進数に対してのみ有効です")
            }

            return tokenOf(
                type,
                sb.toString(),
                start,
                end
            )
        }

        return tokenOf(
            radix.apply(signed),
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
            // NEVER HAPPENS?
            return invalid(start..start, TokenType.DOUBLE_QUOTED, "文字列の初めにはクォートが必要ですが、'${peek()}' が見つかりました")
        }

        val quote = peek()
        // sb.append(quote)
        next()

        if (isOver()) {
            return invalid(start..start, TokenType.DOUBLE_QUOTED, "文字列の解析に失敗しました: 文字列が $quote で閉じられていません")
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
                return invalid(start..end, TokenType.DOUBLE_QUOTED, "文字列の解析に失敗しました: 文字列が $quote で閉じられていません: $sb")
            }

            if (peek() == quote) {
                // クォートだったら解析終了
                // sb.append(peek())

                end = getCurrentSourcePosition()
                next()
                break
            }
            else if (peek() == KeyChar.LINEBREAK) {
                next()
                end = getSourcePositionAtBack()
                return invalid(start..end, TokenType.DOUBLE_QUOTED, "文字列の解析に失敗しました: 文字列が $quote で閉じられていません; 文字列内で改行するには '${KeyChar.ESCAPE}' を使用してください: $sb")
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
    }

    private fun skipComment() {
        if (!interpretComments) return

        if (isOver()) return

        val start = getCurrentSourcePosition()

        if (testThenAdvance(KeyChar.BLOCK_COMMENT_START_SEQUENCE)) {
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

            val end = getSourcePositionAtBack()
            commentRangeCollector.add(start..end)
        }
        else if (testThenAdvance(KeyChar.LINE_COMMENT_SEQUENCE)) {
            while (!isOver()) {
                if (peek() == KeyChar.LINEBREAK) break
                next()
            }

            val end = getSourcePositionAtBack()
            commentRangeCollector.add(start..end)
        }
    }

    private fun word(): Token {
        val sb = StringBuilder()

        val start = getCurrentSourcePosition()

        var current = peek()

        // TODO
        if (current !in KeyChar.SYMBOLS && current !in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<Radix.DECIMAL.value)) {
            sb.append(current)
        }
        else {
            next()
            val end = getSourcePositionAtBack()
            return invalid(start..end, TokenType.WORD, "識別子として無効な文字です: '$current'")
        }

        var end = getCurrentSourcePosition()
        next()

        while (!isOver()) {
            current = peek()

            if (current !in KeyChar.SYMBOLS) {
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
            val end = getSourcePositionAtBack()
            return invalid(start..end, type, "座標入力は $marker で開始されることが期待されましたが、 ${peek()} が見つかりました")
        }

        next()

        if (isOver()) {
            val end = getSourcePositionAtBack()
            return tokenOf(type, "0", start, end)
        }

        if (peek() == KeyChar.MINUS || peek() in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<Radix.DECIMAL.value)) {
            val numToken = number()

            if (numToken.type == TokenType.UINT) {
                val end = getSourcePositionAtBack()
                return invalid(start..end, type, "位置成分として無効なトークンです: '${numToken.sourceSpan.getStringFromSourceCode(sourceCode)}'")
            }

            val number = numToken.value

            val end = getSourcePositionAtBack()

            return tokenOf(type, number, start, end)
        }
        else {
            return tokenOf(type, KeyChar.ZERO.toString(), start, start)
        }
    }

    private fun token(previous: Token?): Token? {
        val result = skipWhitespace()

        val start = getCurrentSourcePosition()

        val noSeparator = previous != null
            && previous.type != TokenType.SYMBOL
            && previous.type != TokenType.COORDINATE_COMPONENT_LOCAL
            && previous.type != TokenType.COORDINATE_COMPONENT_RELATIVE
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
                next()
                val end = getSourcePositionAtBack()
                return invalid(start..end, TokenType.DOUBLE_QUOTED, "区切りが必要です")
            }

            return string()
        }

        if ((peek() == KeyChar.MINUS && peek(1) in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<Radix.DECIMAL.value)) || peek() in KeyChar.NUMBER_CHAR_SEQUENCE.slice(0..<Radix.DECIMAL.value)) {
            return number()
        }

        if (peek() == KeyChar.COORDINATE_RELATIVE) {
            return coordinate(KeyChar.COORDINATE_RELATIVE, TokenType.COORDINATE_COMPONENT_RELATIVE)
        }

        if (peek() == KeyChar.COORDINATE_LOCAL) {
            return coordinate(KeyChar.COORDINATE_LOCAL, TokenType.COORDINATE_COMPONENT_LOCAL)
        }

        if (testThenAdvance(Symbol.AT.value)) {
            for (symbol in Symbol.entries) {
                if (test(symbol.value)) {
                    return tokenOf(Symbol.AT, start, getSourcePositionAtBack())
                }
            }

            val word = word()
            return tokenOf(TokenType.WORD, "${Symbol.AT.value}${word.value}", start, getSourcePositionAtBack())
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
            word()
            val end = getSourcePositionAtBack()
            return invalid(start..end, TokenType.WORD, "トークン $previous の直後には区切りが必要です")
        }

        return word()
    }

    fun tokenize(): List<Token> {
        val list = mutableListOf<Token>()

        try {
            var token: Token? = null

            while (!isOver()) {
                token = token(token) ?: break
                list.add(token)
            }

            // list.add(tokenOf(Symbol.SEMICOLON))

            return list
        }
        catch (error: BclTokenizationException) {
            list.add(invalid(error.sourceSpan, TokenType.UNKNOWN, error.description))
            return list
        }
        finally {
            cursor = 0
            line = 1
            lastLineStartCursor = 0
        }
    }
}
