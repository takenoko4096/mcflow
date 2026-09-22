package io.github.takenoko4096.bcl.reader

import io.github.takenoko4096.bcl.brigadier.value.location.CoordinateComponent
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.brigadier.value.misc.TimeDuration
import io.github.takenoko4096.bcl.brigadier.value.misc.TimeUnit
import io.github.takenoko4096.bcl.parser.BclParseException
import io.github.takenoko4096.bcl.parser.Keyword
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType

open class TokenSeqReader(
    val sourceCode: String,
    protected val tokens: List<Token>,
    private val exceptionConstructor: (message: String, text: String, span: SourceSpan, cause: Throwable?) -> BclTokenSeqReadException
) {
    var cursor: Int = 0

    fun clone(): TokenSeqReader {
        val cpy = TokenSeqReader(sourceCode, tokens, exceptionConstructor)
        cpy.cursor = cursor
        return cpy
    }

    fun getRemainingSourceSpan(): SourceSpan {
        return getCurrentTokenStartPosition()..tokens.last().sourceSpan.end
    }

    fun isOver(): Boolean {
        return cursor >= tokens.size
    }

    fun peek(): Token {
        if (isOver()) {
            throw exception(getCurrentTokenStartPosition(), "入力が不足しています")
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

    fun testThenAdvance(predicate: (Token) -> Boolean, preAdvance: (() -> Unit)? = null, postAdvance: (() -> Unit)? = null): Boolean {
        val result = predicate(peek())
        return if (result) {
            preAdvance?.invoke()
            next()
            postAdvance?.invoke()
            true
        }
        else {
            false
        }
    }

    fun testThenAdvanceIgnoreNewLine(predicate: (Token) -> Boolean, preAdvance: (() -> Unit)? = null, postAdvance: (() -> Unit)? = null): Boolean {
        val result = predicate(peekIgnoreNewLine())
        return if (result) {
            preAdvance?.invoke()
            nextIgnoreNewLine()
            postAdvance?.invoke()
            true
        }
        else {
            false
        }
    }

    fun getWhitespaceBetweenPreviousAndCurrent(): String{
        val from = getPreviousTokenEndPosition().index + 1
        val to = getCurrentTokenStartPosition().index - 1
        return sourceCode.slice(from..to)
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
        if (types.isEmpty()) {
            throw IllegalArgumentException("read() を引数無しで呼び出す馬鹿がどこにいる？")
        }

        if (isOver()) {
            throw exception(peekSafely(-1).sourceSpan, "$types のうちいずれかのトークンが期待されましたが、入力が不足しています")
        }

        val start = getCurrentTokenStartPosition()
        val current = peek()
        if (current.type in types) {
            next()
            return current.value
        }
        else {
            next()
            throw exception(
                start,
                if (types.size == 1) {
                    "${types.first()} のトークンが期待されましたが、 ${current.type} のトークン '${current.value}' が見つかりました"
                }
                else {
                    "${types.contentToString()} のうちいずれかのトークンが期待されましたが、 ${current.type} のトークン '${current.value}' が見つかりました"
                }
            )
        }
    }

    fun readIgnoreNewLine(vararg types: TokenType): String {
        if (isOverIgnoreNewLine()) {
            throw exception(peekSafely(-1).sourceSpan, "${types.contentToString()} のうちいずれかのトークンが期待されましたが、コード外に到達しました")
        }

        val start = getCurrentTokenStartPosition()
        val current = peekIgnoreNewLine()
        if (current.type in types) {
            nextIgnoreNewLine()
            return current.value
        }
        else {
            nextIgnoreNewLine()
            throw exception(start, "${types.contentToString()} のうちいずれかのトークンが期待されましたが、トークンの種類が一致しませんでした: ${current.type} ($current)")
        }
    }

    fun getCurrentTokenStartPosition(): SourcePosition {
        return peekSafely(0).sourceSpan.start
    }

    fun getPreviousTokenEndPosition(): SourcePosition {
        return peekSafely(-1).sourceSpan.end
    }

    fun readInteger(signedDecimalOnly: Boolean = true): Int {
        if (isOver()) {
            throw exception(peekSafely(-1).sourceSpan, "整数が期待されましたが、入力が不足しています")
        }

        val start = getCurrentTokenStartPosition()

        val u = peek()

        val v = when (u.type) {
            TokenType.INT_RAD2 -> u.value.toInt(2)
            TokenType.INT_RAD8 -> u.value.toInt(8)
            TokenType.INT -> u.value.toInt()
            TokenType.INT_RAD16 -> u.value.toInt(16)
            TokenType.UINT_RAD2 -> u.value.toUInt(2).toInt()
            TokenType.UINT_RAD8 -> u.value.toUInt(8).toInt()
            TokenType.UINT -> u.value.toUInt().toInt()
            TokenType.UINT_RAD16 -> u.value.toUInt(16).toInt()
            else -> {
                next()
                throw exception(start, "整数の読み取りに失敗しました: 不明なトークンです: '${u.sourceSpan.getStringFromSourceCode(sourceCode)}'")
            }
        }

        next()

        if (signedDecimalOnly && u.type != TokenType.INT) {
            throw exception(start, "整数の読み取りに失敗しました: 不明なトークンです: '${u.sourceSpan.getStringFromSourceCode(sourceCode)}'")
        }

        return v
    }

    fun readTimeDuration(): TimeDuration {
        if (isOver()) {
            throw exception(peekSafely(-1).sourceSpan, "時間が期待されましたが、入力が不足しています")
        }

        val start = getCurrentTokenStartPosition()

        val u = peek()

        val v = when (u.type) {
            TokenType.TIME_D -> TimeDuration(u.value.toInt(), TimeUnit.DAY)
            TokenType.TIME_S -> TimeDuration(u.value.toInt(), TimeUnit.SECOND)
            TokenType.TIME_T, TokenType.INT -> TimeDuration(u.value.toInt(), TimeUnit.TICK)
            else -> {
                next()
                throw exception(start, "時間の読み取りに失敗しました: 不明なトークンです: '${u.sourceSpan.getStringFromSourceCode(sourceCode)}'")
            }
        }

        next()

        return v
    }

    fun readFloat(): Float {
        if (isOver()) {
            throw exception(peekSafely(-1).sourceSpan.end, "浮動小数点数が期待されましたが、入力が不足しています")
        }

        return read(TokenType.FLOAT, TokenType.INT).toFloat()
    }

    fun readQuotedString(): String {
        return read(TokenType.DOUBLE_QUOTED)
    }

    fun readWord(): String {
        return read(TokenType.WORD)
    }

    fun readBool(): Boolean {
        val start = getCurrentTokenStartPosition()
        val word = readWord()

        next()
        return when (word) {
            "true" -> true
            "false" -> false
            else -> {
                throw exception(start, "真偽値の読み取りに失敗しました: 不明なトークンです: $word")
            }
        }
    }

    fun readCoordinateComponent(blockCenterCorrection: Boolean = false, allowLocal: Boolean = true): CoordinateComponent {
        val start = getCurrentTokenStartPosition()
        val t = peek()

        when (t.type) {
            TokenType.INT -> {
                next()
                return CoordinateComponent.Absolute(t.value.toFloat() + if (blockCenterCorrection) 0.5f else 0f)
            }
            TokenType.FLOAT -> {
                next()
                return CoordinateComponent.Absolute(t.value.toFloat())
            }
            TokenType.COORDINATE_RELATIVE -> {
                next()
                return CoordinateComponent.Relative(t.value.toFloat())
            }
            TokenType.COORDINATE_LOCAL -> {
                next()
                if (allowLocal) {
                    return CoordinateComponent.Local(t.value.toFloat())
                }
                else {
                    val end = getPreviousTokenEndPosition()
                    throw exception(start..end, "位置成分の読み取りに失敗しました: ここではローカル座標系は許可されていません")
                }
            }
            else -> {
                next()
                throw exception(
                    start,
                    "位置成分の読み取りに失敗しました: ${TokenType.INT}, ${TokenType.FLOAT}, ${TokenType.COORDINATE_RELATIVE}, ${TokenType.COORDINATE_LOCAL} のうちいずれかのトークンが期待されましたが、 ${t.type} のトークン '${t.value}' が渡されました"
                )
            }
        }
    }

    fun readNamespacedIdentifier(quotable: Boolean = false, defaultNamespace: String? = NamespacedIdentifier.DEFAULT_NAMESPACE, strict: Boolean = true): NamespacedIdentifier {
        val start = getCurrentTokenStartPosition()
        val string = if (quotable) readQuotableCommandString() else readCommandString()
        val end = getPreviousTokenEndPosition()

        val identifier = try {
            NamespacedIdentifier.parse(string, defaultNamespace)
                ?: run {
                    if (strict) {
                        throw exception(start..end, "識別子の読み取りに失敗しました: 文字列 '$string' は識別子として無効です (識別子は '${NamespacedIdentifier.REGEX}' の形式に従う必要があります)")
                    }
                    else if (defaultNamespace != null) {
                        NamespacedIdentifier(defaultNamespace, string)
                    }
                    else {
                        throw exception(start..end, "識別子の読み取りに失敗しました: 文字列 '$string' は識別子として無効です (非厳格モードですが、フォールバックの名前空間が指定されていません)")
                    }
                }
        }
        catch (error: IllegalArgumentException) {
            throw exception(start..end, "識別子の読み取りに失敗しました: 名前空間が要求されていますが、名前空間が見つかりませんでした", error)
        }

        return identifier
    }

    fun readAllRemainingAsString(): String {
        val remainingSpan = getRemainingSourceSpan()
        val string = sourceCode.slice(remainingSpan.start.index..remainingSpan.end.index)
        cursor = tokens.size
        return string
    }

    fun exception(sourceSpan: SourceSpan, message: String, cause: Throwable? = null): BclTokenSeqReadException {
        return exceptionConstructor(message, sourceCode, sourceSpan, cause)
    }

    fun exception(start: SourcePosition, message: String, cause: Throwable? = null): BclTokenSeqReadException {
        return exceptionConstructor(message, sourceCode, start..getPreviousTokenEndPosition(), cause)
    }

    protected fun createCommandLineSubReader(): TokenSeqReader {
        val tokens = mutableListOf<Token>()
        var current: Token

        skipNewLine()

        while (!isOver()) {
            current = peek()
            next()

            if (current `is` Symbol.ESCAPE && peek() `is` Symbol.NEWLINE) {
                next()
                continue
            }

            if (current `is` Symbol.NEWLINE) {
                break
            }

            tokens.add(current)
        }

        return TokenSeqReader(sourceCode, tokens,::BclParseException)
    }

    fun readIntRange(): IntRange {
        val start = getCurrentTokenStartPosition()
        val t = peek()
        return if (t `is` Symbol.RANGE) {
            next()
            val min = Int.MIN_VALUE
            val max = readInteger()
            min..max
        }
        else if (t.type == TokenType.INT) {
            next()
            val min = t.value.toInt()
            val max = if (!isOver() && peek() `is` Symbol.RANGE) {
                next()
                if (isOver()) {
                    Int.MAX_VALUE
                }
                else {
                    val t = peek()
                    if (t.type == TokenType.INT) {
                        next()
                        t.value.toInt()
                    }
                    else Int.MAX_VALUE
                }
            }
            else min
            min..max
        }
        else {
            next()
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "整数範囲の読み取りに失敗しました: 不明なトークンです: $t")
        }
    }

    fun <V> readList(prefix: Symbol, postfix: Symbol, elementParser: TokenSeqReader.() -> V): List<V> {
        val list = mutableListOf<V>()

        val start = getCurrentTokenStartPosition()
        expect(prefix)

        if (peek() `is` postfix) {
            next()
            return list
        }

        while (true) {
            val elem = elementParser()
            list.add(elem)

            if (isOver()) {
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "リストの読み取りに失敗しました: '$postfix' で閉じられていません")
            }
            else if (peek() `is` Symbol.COMMA) {
                next()
            }
            else if (peek() `is` postfix) {
                next()
                break
            }
            else {
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "リストの読み取りに失敗しました: 不明なトークンです: ${peek()}")
            }
        }

        return list
    }

    fun <K : Any, S, V> readPairList(prefix: Symbol, postfix: Symbol, kvSeparatorParser: TokenSeqReader.() -> S, keyParser: TokenSeqReader.() -> K?, valueParser: TokenSeqReader.(S) -> V): List<Pair<K, V>> {
        val list = mutableListOf<Pair<K, V>>()

        val start = getCurrentTokenStartPosition()
        expect(prefix)

        if (peek() `is` postfix) {
            next()
            return list
        }

        while (true) {
            val key = keyParser()

            if (key != null) {
                val separator = kvSeparatorParser()

                val value = valueParser(separator)
                list.add(key to value)
            }

            if (isOver()) {
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "ペアリストの読み取りに失敗しました: '$postfix' で閉じられていません")
            }
            else if (peek() `is` Symbol.COMMA) {
                next()
            }
            else if (peek() `is` postfix) {
                next()
                break
            }
            else {
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "ペアリストの読み取りに失敗しました: 不明なトークンです: ${peek()}")
            }
        }

        return list
    }

    fun <K : Any, S, V> readMap(prefix: Symbol, postfix: Symbol, kvSeparatorParser: TokenSeqReader.() -> S, keyParser: TokenSeqReader.() -> K?, valueParser: TokenSeqReader.(S) -> V): Map<K, V> {
        val map = mutableMapOf<K, V>()

        val start = getCurrentTokenStartPosition()
        expect(prefix)

        if (peek() `is` postfix) {
            next()
            return map
        }

        while (true) {
            val key = keyParser()

            if (key != null) {
                val separator = kvSeparatorParser()

                val value = valueParser(separator)
                map[key] = value
            }

            if (isOver()) {
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "マップの読み取りに失敗しました: '$postfix' で閉じられていません")
            }
            else if (peek() `is` Symbol.COMMA) {
                next()
            }
            else if (peek() `is` postfix) {
                next()
                break
            }
            else {
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "マップの読み取りに失敗しました: 不明なトークンです: ${peek()}")
            }
        }

        return map
    }

    fun <K, S, V> readTypedMap(prefix: Symbol, postfix: Symbol, kvSeparatorParser: TokenSeqReader.() -> S, keyParser: TokenSeqReader.() -> K, valueParsers: Map<K, TokenSeqReader.(S) -> V>): Map<K, V> {
        val map = mutableMapOf<K, V>()

        val start = getCurrentTokenStartPosition()
        expect(prefix)

        if (peek() `is` postfix) {
            next()
            return map
        }

        while (true) {
            val key = keyParser()
            val valueParser = valueParsers[key]
                ?: run {
                    val end = getPreviousTokenEndPosition()
                    val range = start..end
                    throw exception(range, "マップオブジェクトの読み取りに失敗しました: 不明なキーです: ${range.getStringFromSourceCode(sourceCode)}")
                }

            val separator = kvSeparatorParser()

            val value = valueParser(separator)
            map[key] = value

            if (isOver()) {
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "マップオブジェクトの読み取りに失敗しました: '$postfix' で閉じられていません")
            }
            else if (peek() `is` Symbol.COMMA) {
                next()
            }
            else if (peek() `is` postfix) {
                next()
                break
            }
            else {
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "マップオブジェクトの読み取りに失敗しました: 不明なトークンです: ${peek()}")
            }
        }

        return map
    }

    fun <K, S, V> readTypedMapIgnoreNewLine(prefix: Symbol, postfix: Symbol, kvSeparatorParser: TokenSeqReader.() -> S, keyParser: TokenSeqReader.() -> K, valueParsers: Map<K, TokenSeqReader.(S) -> V>): Map<K, V> {
        val map = mutableMapOf<K, V>()

        val start = getCurrentTokenStartPosition()
        expectIgnoreNewLine(prefix)

        if (peekIgnoreNewLine() `is` postfix) {
            nextIgnoreNewLine()
            return map
        }

        while (true) {
            val key = keyParser()
            val valueParser = valueParsers[key]
                ?: run {
                    val end = getPreviousTokenEndPosition()
                    val range = start..end
                    throw exception(range, "マップオブジェクトの読み取りに失敗しました: 不明なキーです: ${range.getStringFromSourceCode(sourceCode)}")
                }

            val separator = kvSeparatorParser()

            val value = valueParser(separator)
            map[key] = value

            if (isOverIgnoreNewLine()) {
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "マップオブジェクトの読み取りに失敗しました: '$postfix' で閉じられていません")
            }
            else if (peekIgnoreNewLine() `is` Symbol.COMMA) {
                nextIgnoreNewLine()
            }
            else if (peekIgnoreNewLine() `is` postfix) {
                nextIgnoreNewLine()
                break
            }
            else {
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "マップオブジェクトの読み取りに失敗しました: 不明なトークンです: ${peekIgnoreNewLine()}")
            }
        }

        return map
    }

    fun readCommandString(allowFloat: Boolean = false, allowSingleSignOnly: Boolean = false): String {
        if (isOver()) {
            throw exception(peekSafely(-1).sourceSpan, "文字列の解析に失敗しました: 入力が不足しています")
        }

        val start = getCurrentTokenStartPosition()

        val startIndex = peek().sourceSpan.start.index

        val firstChar = sourceCode.getOrNull(startIndex)
        val secondChar = sourceCode.getOrNull(startIndex + 1)

        var index = startIndex

        if (firstChar == KeyChar.COLON) {
            next()
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "文字列の解析に失敗しました: 文字列は '${KeyChar.COLON}' で開始できません")
        }

        if (firstChar == KeyChar.WILDCARD) {
            next()
            return firstChar.toString()
        }

        if (firstChar in setOf(KeyChar.PLUS, KeyChar.MINUS)) {
            if (secondChar == null || secondChar in KeyChar.COMMAND_WORD_TERMINATORS) {
                next()

                if (allowSingleSignOnly) {
                    return firstChar.toString()
                }

                val end = getPreviousTokenEndPosition()

                throw exception(start..end, "文字列の解析に失敗しました: 文字列は $firstChar で開始された場合直後にターミネータ文字または終端があってはなりません")
            }

            index++
        }

        while (sourceCode[index] !in KeyChar.COMMAND_WORD_TERMINATORS) {
            index++
            if (index >= sourceCode.length) break
        }

        val string = sourceCode.substring(startIndex, index)

        cursor = tokens.indexOfFirst { it.sourceSpan.start.index >= index }

        if (cursor == -1) {
            cursor = tokens.size
        }

        val end = getPreviousTokenEndPosition()

        if (string.isEmpty()) {
            throw exception(peekSafely(0).sourceSpan, "文字列の解析に失敗しました: 無効なトークンです: ${peekSafely(0)}")
        }
        else if (!allowFloat && string.matches("^\\d+(?:\\.\\d+)?$".toRegex())) {
            throw exception(start..end, "文字列の解析に失敗しました: 数値として構成される文字列は不正です")
        }
        else if (string.matches("^\\.$".toRegex()) && string.length % 2 == 0) {
            throw exception(start..end, "文字列の解析に失敗しました: 文字列は偶数個の '${KeyChar.DOT}' に等しくあってはなりません")
        }

        return string
    }

    fun readQuotableCommandString(): String {
        if (isOver()) {
            throw exception(peekSafely(-1).sourceSpan, "二重引用符付き文字列または文字列の解析に失敗しました: 入力が不足しています")
        }

        return if (peek().type == TokenType.DOUBLE_QUOTED) {
            readQuotedString()
        }
        else {
            readCommandString()
        }
    }

    fun readJsonValue(): CommandJsonValue<*> {
        val vStart = getCurrentTokenStartPosition()
        val t = peek()
        return when (t.type) {
            TokenType.WORD -> {
                next()
                val vEnd = getPreviousTokenEndPosition()
                when (t.value) {
                    "true" -> CommandJsonValue(true, vStart..vEnd)
                    "false" -> CommandJsonValue(false, vStart..vEnd)
                    "null" -> CommandJsonValue(null, vStart..vEnd)
                    else -> throw exception(vStart..vEnd, "json の読み取りに失敗しました: 不正な値です: ${t.value}")
                }
            }
            TokenType.INT -> {
                next()
                val vEnd = getPreviousTokenEndPosition()
                CommandJsonValue(t.value.toInt(), vStart..vEnd)
            }
            TokenType.FLOAT -> {
                next()
                val vEnd = getPreviousTokenEndPosition()
                CommandJsonValue(t.value.toFloat(), vStart..vEnd)
            }
            TokenType.DOUBLE_QUOTED -> {
                next()
                val vEnd = getPreviousTokenEndPosition()
                CommandJsonValue(t.value, vStart..vEnd)
            }
            TokenType.SYMBOL -> {
                if (t `is` Symbol.BRACE_START) {
                    readJsonObject()
                }
                else if (t `is` Symbol.ARRAY_START) {
                    readJsonArray()
                }
                else {
                    next()
                    val vEnd = getPreviousTokenEndPosition()
                    throw exception(vStart..vEnd, "json の読み取りに失敗しました: 不正な値です: ${t.value}")
                }
            }
            else -> {
                next()
                val vEnd = getPreviousTokenEndPosition()
                throw exception(vStart..vEnd, "json の読み取りに失敗しました: 不正な値です: ${t.value}")
            }
        }
    }

    fun readJsonArray(): CommandJsonArray {
        val list = mutableListOf<CommandJsonValue<*>>()

        val start = getCurrentTokenStartPosition()

        expect(Symbol.ARRAY_START)

        if (peek() `is` Symbol.ARRAY_END) {
            next()
            val end = getPreviousTokenEndPosition()
            return CommandJsonArray(list, start..end)
        }

        while (true) {
            val element = readJsonValue()

            list.add(element)

            if (isOver()) {
                throw exception(peekSafely(-1).sourceSpan, "json の読み取りに失敗しました: 入力が不足しています")
            }

            val t = peek()

            if (t `is` Symbol.COMMA) {
                next()
            }
            else if (t `is` Symbol.ARRAY_END) {
                next()
                break
            }
            else {
                throw exception(t.sourceSpan, "json の読み取りに失敗しました: 無効なトークンです: $t")
            }
        }

        val end = getPreviousTokenEndPosition()

        return CommandJsonArray(list, start..end)
    }

    fun readJsonObject(): CommandJsonObject {
        val entries = mutableListOf<Pair<CommandJsonValue<String>, CommandJsonValue<*>>>()

        val start = getCurrentTokenStartPosition()

        expect(Symbol.BRACE_START)

        if (peek() `is` Symbol.BRACE_END) {
            next()
            val end = getPreviousTokenEndPosition()
            return CommandJsonObject(entries, start..end)
        }

        while (true) {
            val kStart = getCurrentTokenStartPosition()
            val key = readQuotedString()
            val kEnd = getPreviousTokenEndPosition()
            expect(Symbol.COLON)

            val value = readJsonValue()

            entries.add(CommandJsonValue(key, kStart..kEnd) to value)

            if (isOver()) {
                throw exception(peekSafely(-1).sourceSpan, "json の読み取りに失敗しました: 入力が不足しています")
            }

            val t = peek()
            if (t `is` Symbol.COMMA) {
                next()
            }
            else if (t `is` Symbol.BRACE_END) {
                next()
                break
            }
            else {
                throw exception(t.sourceSpan, "json の読み取りに失敗しました: 無効なトークンです: $t")
            }
        }

        val end = getPreviousTokenEndPosition()

        return CommandJsonObject(entries, start..end)
    }
}
