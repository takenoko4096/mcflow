package io.github.takenoko4096.bcl.reader

import io.github.takenoko4096.bcl.WithSpan
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.location.CoordinateComponent
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.brigadier.value.misc.TimeDuration
import io.github.takenoko4096.bcl.brigadier.value.misc.TimeUnit
import io.github.takenoko4096.bcl.parser.BclParseException
import io.github.takenoko4096.bcl.parser.Keyword
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType

open class TokenSeqReader(
    val sourceCode: String,
    protected val tokens: List<Token>,
    private val exceptionConstructor: (message: String, text: String, span: SourceSpan, cause: Throwable?, unfatalizable: Boolean) -> BclTokenSeqReadException
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
            throw exception(getOverNextSourceSpan(), "入力が不足しています")
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
        if (isOver()) return false
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
        if (isOverIgnoreNewLine()) return false
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

        if (isOver()) {
            throw exception(getOverNextSourceSpan(), "'${symbol.value}' が期待されていますが、入力が不足しています")
        }

        if (peek() `is` symbol) {
            next()
        }
        else {
            val v = peek().value
            next()
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "予期しないトークンです: $v; ${symbol.value} が期待されています")
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
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "入力が不足しています")
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

        if (isOverIgnoreNewLine()) {
            throw exception(getOverNextSourceSpan(), "'${symbol.value}' が期待されていますが、入力が不足しています")
        }

        if (peekIgnoreNewLine() `is` symbol) {
            nextIgnoreNewLine()
        }
        else {
            val v = peekIgnoreNewLine().value
            nextIgnoreNewLine()
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "予期しないトークンです: $v; ${symbol.value} が期待されています")
        }
    }

    fun expectIgnoreNewLine(keyword: Keyword) {
        val start = getCurrentTokenStartPosition()

        if (isOverIgnoreNewLine()) {
            throw exception(getOverNextSourceSpan(), "'${keyword.value}' が期待されていますが、入力が不足しています")
        }

        if (isOverIgnoreNewLine()) {
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "トークン ${keyword.value} が期待されましたが、入力が不足しています")
        }

        if (peekIgnoreNewLine() `is` keyword) {
            nextIgnoreNewLine()
        }
        else {
            val v = peekIgnoreNewLine().value
            nextIgnoreNewLine()
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "予期しないトークンです: $v; ${keyword.value} が期待されています")
        }
    }

    fun read(vararg types: TokenType): String {
        if (types.isEmpty()) {
            throw IllegalArgumentException("read() を引数無しで呼び出す馬鹿がどこにいる？")
        }

        if (isOver()) {
            throw exception(getOverNextSourceSpan(), "${types.joinToString(" | ")} のトークンが期待されましたが、入力が不足しています")
        }

        val start = getCurrentTokenStartPosition()
        val current = peek()
        if (current.type in types) {
            next()
            return current.value
        }
        else {
            next()
            val end = getPreviousTokenEndPosition()
            throw exception(
                start..end,
                if (types.size == 1) {
                    if (current.type == TokenType.INVALID) {
                        "${types.first()} のトークンが期待されましたが、無効なトークンが見つかりました: ${current.value}"
                    }
                    else {
                        "${types.first()} のトークンが期待されましたが、 ${current.type} のトークン '${current.value}' が見つかりました"
                    }
                }
                else {
                    if (current.type == TokenType.INVALID) {
                        "${types.joinToString(" | ")} のトークンが期待されましたが、無効なトークンが見つかりました: ${current.value}"
                    }
                    else {
                        "${types.joinToString(" | ")} のトークンが期待されましたが、 ${current.type} のトークン '${current.value}' が見つかりました"
                    }
                }
            )
        }
    }

    fun readIgnoreNewLine(vararg types: TokenType): String {
        if (isOverIgnoreNewLine()) {
            throw exception(peekSafely(-1).sourceSpan, "${types.joinToString(" | ")} のトークンが期待されましたが、入力が不足しています")
        }

        val start = getCurrentTokenStartPosition()
        val current = peekIgnoreNewLine()
        if (current.type in types) {
            nextIgnoreNewLine()
            return current.value
        }
        else {
            nextIgnoreNewLine()
            val end = getPreviousTokenEndPosition()
            throw exception(
                start..end,
                "${types.joinToString(" | ")} のトークンが期待されましたが、${if (current.type == TokenType.INVALID) "無効なトークンが見つかりました: ${current.value}" else "トークンの種類が一致しませんでした: ${current.type} の '${current.value}' が渡されました"}"
            )
        }
    }

    fun getCurrentTokenStartPosition(): SourcePosition {
        return peekSafely(0).sourceSpan.start
    }

    fun getPreviousTokenEndPosition(): SourcePosition {
        return peekSafely(-1).sourceSpan.end
    }

    fun getOverNextSourceSpan(): SourceSpan {
        val last = tokens.lastOrNull()?.sourceSpan?.end ?: SourcePosition(1, 0, 0)
        val over = SourcePosition(last.line, last.column + 1, last.index + 1)
        return over..over
    }

    fun readInteger(allowExtendedNotation: Boolean = false): Int {
        if (isOver()) {
            throw exception(getOverNextSourceSpan(), "整数が期待されましたが、入力が不足しています")
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
            TokenType.INVALID -> {
                next()
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "整数の読み取りに失敗しました: 無効なトークンです: ${u.value}")
            }
            else -> {
                next()
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "整数の読み取りに失敗しました: 不明なトークンです: '${u.sourceSpan.getStringFromSourceCode(sourceCode)}'")
            }
        }

        next()
        val end = getPreviousTokenEndPosition()

        if (!allowExtendedNotation && u.type != TokenType.INT) {
            throw exception(start..end, "整数の読み取りに失敗しました: 不明なトークンです: '${u.sourceSpan.getStringFromSourceCode(sourceCode)}'")
        }

        return v
    }

    fun readTimeDuration(): TimeDuration {
        if (isOver()) {
            throw exception(getOverNextSourceSpan(), "時間が期待されましたが、入力が不足しています")
        }

        val start = getCurrentTokenStartPosition()

        val u = peek()

        val v = when (u.type) {
            TokenType.TIME_D -> TimeDuration(u.value.toInt(), TimeUnit.DAY)
            TokenType.TIME_S -> TimeDuration(u.value.toInt(), TimeUnit.SECOND)
            TokenType.TIME_T, TokenType.INT -> TimeDuration(u.value.toInt(), TimeUnit.TICK)
            TokenType.INVALID -> {
                next()
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "時間の読み取りに失敗しました: 無効なトークンです: ${u.value}")
            }
            else -> {
                next()
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "時間の読み取りに失敗しました: 不明なトークンです: '${u.sourceSpan.getStringFromSourceCode(sourceCode)}'")
            }
        }

        next()

        return v
    }

    fun readFloat(): Float {
        if (isOver()) {
            throw exception(getOverNextSourceSpan(), "浮動小数点数が期待されましたが、入力が不足しています")
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
        val word = readCommandString()
        val end = getPreviousTokenEndPosition()
        return when (word) {
            "true" -> true
            "false" -> false
            else -> throw exception(start..end, "真偽値の読み取りに失敗しました: 不明なトークンです: $word")
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
            TokenType.COORDINATE_COMPONENT_RELATIVE -> {
                next()
                return CoordinateComponent.Relative(t.value.toFloat())
            }
            TokenType.COORDINATE_COMPONENT_LOCAL -> {
                next()
                if (allowLocal) {
                    return CoordinateComponent.Local(t.value.toFloat())
                }
                else {
                    val end = getPreviousTokenEndPosition()
                    throw exception(start..end, "位置成分の読み取りに失敗しました: ここではローカル座標系は許可されていません", unfatalizable = true)
                }
            }
            TokenType.INVALID -> {
                next()
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "位置成分の読み取りに失敗しました: 無効なトークンです: ${t.value}")
            }
            else -> {
                next()
                val end = getPreviousTokenEndPosition()
                throw exception(
                    start..end,
                    "位置成分の読み取りに失敗しました: ${TokenType.INT} | ${TokenType.FLOAT} | ${TokenType.COORDINATE_COMPONENT_RELATIVE} | ${TokenType.COORDINATE_COMPONENT_LOCAL} のトークンが期待されましたが、 ${t.type} の '${t.value}' が渡されました"
                )
            }
        }
    }

    fun readNamespacedIdentifier(defaultNamespace: String? = NamespacedIdentifier.DEFAULT_NAMESPACE, quotable: Boolean = false, strict: Boolean = true): NamespacedIdentifier {
        val start = getCurrentTokenStartPosition()
        val string = if (quotable) readQuotableCommandString() else readCommandString()
        val end = getPreviousTokenEndPosition()

        val identifier = try {
            NamespacedIdentifier.parse(string, defaultNamespace)
        }
        catch (error: IllegalArgumentException) {
            throw exception(start..end, "識別子の読み取りに失敗しました: 名前空間が要求されていますが、名前空間が見つかりませんでした", error, unfatalizable = true)
        }

        return identifier
            ?: run {
                if (strict) {
                    throw exception(start..end, "識別子の読み取りに失敗しました: 文字列 '$string' は識別子として無効です (識別子は '${NamespacedIdentifier.REGEX}' の形式に従う必要があります)", unfatalizable = true)
                }

                NamespacedIdentifier(defaultNamespace ?: NamespacedIdentifier.DEFAULT_NAMESPACE, string)
            }
    }

    fun readAllRemainingAsString(): String {
        val remainingSpan = getRemainingSourceSpan()
        val string = sourceCode.slice(remainingSpan.start.index..remainingSpan.end.index)
        cursor = tokens.size
        return string
    }

    fun exception(sourceSpan: SourceSpan, message: String, cause: Throwable? = null, unfatalizable: Boolean = false): BclTokenSeqReadException {
        return exceptionConstructor(message, sourceCode, sourceSpan, cause, unfatalizable)
    }

    @Deprecated(message = "Please pass a source span instead of source position to the first argument.")
    fun exception(start: SourcePosition, message: String, cause: Throwable? = null, unfatalizable: Boolean = false): BclTokenSeqReadException {
        return exceptionConstructor(message, sourceCode, start..getPreviousTokenEndPosition(), cause, unfatalizable)
    }

    fun diagnostic(sourceSpan: SourceSpan, message: String): BclDiagnostic {
        return BclDiagnostic(message, sourceCode, sourceSpan)
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

        return TokenSeqReader(sourceCode, tokens, ::BclParseException)
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
        else if (t.type == TokenType.INVALID) {
            next()
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "整数範囲の読み取りに失敗しました: 無効なトークンです: ${t.value}")
        }
        else {
            next()
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "整数範囲の読み取りに失敗しました: 不明なトークンです: $t")
        }
    }

    fun <V> readList(prefix: Symbol, postfix: Symbol, elementParser: TokenSeqReader.() -> V?): List<V> {
        val list = mutableListOf<V>()

        val start = getCurrentTokenStartPosition()
        expect(prefix)

        if (isOver()) {
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "リストの読み取りに失敗しました: リストが '${postfix.value}' で閉じられていません")
        }

        if (peek() `is` postfix) {
            next()
            return list
        }

        while (true) {
            val elem = elementParser()
            if (elem != null) list.add(elem)

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
            else if (peek().type == TokenType.INVALID) {
                val t = peek()
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "リストの読み取りに失敗しました: 無効なトークンです: ${t.value}")
            }
            else {
                val t = peek()
                next()
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "リストの読み取りに失敗しました: 不明なトークンです: $t")
            }
        }

        return list
    }

    fun <K : Any, S, V> readDuplicatableMap(prefix: Symbol, postfix: Symbol, kvSeparatorParser: TokenSeqReader.() -> S, keyParser: TokenSeqReader.() -> K?, valueParser: TokenSeqReader.(K?, S) -> V?): List<Pair<K, V>> {
        val list = mutableListOf<Pair<K, V>>()

        val start = getCurrentTokenStartPosition()
        expect(prefix)

        if (isOver()) {
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "マップオブジェクトの読み取りに失敗しました: マップオブジェクトが '${postfix.value}' で閉じられていません")
        }

        if (peek() `is` postfix) {
            next()
            return list
        }

        while (true) {
            val key = keyParser()

            val separator = kvSeparatorParser()

            val value = valueParser(key, separator)

            if (key != null && value != null) {
                list.add(key to value)
            }

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
            else if (peek().type == TokenType.INVALID) {
                val t = peek()
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "マップオブジェクトの読み取りに失敗しました: 無効なトークンです: ${t.value}")
            }
            else {
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "マップオブジェクトの読み取りに失敗しました: 不明なトークンです: ${peek()}")
            }
        }

        return list
    }

    fun <K, V> readMap(prefix: Symbol, postfix: Symbol, keyParser: TokenSeqReader.() -> K, kvSeparatorParser: TokenSeqReader.() -> Unit, valueParser: TokenSeqReader.() -> V): Map<K, Pair<SourceSpan, WithSpan<V>>> {
        val list = mutableListOf<Pair<WithSpan<K>, WithSpan<V>>>()

        expect(prefix)

        if (isOver()) {
            throw exception(getOverNextSourceSpan(), "マップオブジェクトの読み取りに失敗しました: マップオブジェクトが '${postfix.value}' で閉じられていません")
        }

        if (peek() `is` postfix) {
            next()
            return mapOf()
        }

        while (true) {
            val keyStart = getCurrentTokenStartPosition()
            val key = keyParser()
            val keyEnd = getPreviousTokenEndPosition()

            if (isOver()) {
                throw exception(getOverNextSourceSpan(), "マップの読み取りに失敗しました: キーと値の間には区切りが必要です")
            }

            kvSeparatorParser()

            if (isOver()) {
                throw exception(getOverNextSourceSpan(), "マップの読み取りに失敗しました: 区切りの直後に値が欠落しています")
            }

            val valueStart = getCurrentTokenStartPosition()
            val value = valueParser()
            val valueEnd = getPreviousTokenEndPosition()

            list.add(WithSpan(key, keyStart..keyEnd) to WithSpan(value, valueStart..valueEnd))

            if (isOver()) {
                throw exception(getOverNextSourceSpan(), "マップの読み取りに失敗しました: '$postfix' で閉じられていません")
            }

            val t = peek()
            if (t `is` Symbol.COMMA) {
                next()
            }
            else if (t `is` postfix) {
                next()
                break
            }
            else if (t.type == TokenType.INVALID) {
                throw exception(t.sourceSpan, "マップの読み取りに失敗しました: 無効なトークンです: ${t.value}")
            }
            else {
                throw exception(t.sourceSpan, "マップの読み取りに失敗しました: 不明なトークンです: $t")
            }
        }

        val map = mutableMapOf<K, Pair<SourceSpan, WithSpan<V>>>()

        for ((key, value) in list) {
            if (key.value in map) {
                throw exception(key.sourceSpan, "マップの読み取りに失敗しました: キー '${key.value}' が重複しています")
            }

            map[key.value] = key.sourceSpan to value
        }

        return map
    }

    fun <K, S, V> readTypedMap(prefix: Symbol, postfix: Symbol, kvSeparatorParser: TokenSeqReader.() -> S, keyParser: TokenSeqReader.() -> K, valueParsers: Map<K, TokenSeqReader.(S) -> V>, fallbackValueParser: TokenSeqReader.(WithSpan<K>, S) -> Unit): Map<K, V> {
        val map = mutableMapOf<K, V>()

        val start = getCurrentTokenStartPosition()
        expect(prefix)

        if (isOver()) {
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "マップオブジェクトの読み取りに失敗しました: マップオブジェクトが '${postfix.value}' で閉じられていません")
        }

        if (peek() `is` postfix) {
            next()
            return map
        }

        while (true) {
            val keyStart = getCurrentTokenStartPosition()
            val key = keyParser()
            val keyEnd = getPreviousTokenEndPosition()
            val separator = kvSeparatorParser()
            val valueParser = valueParsers[key]

            if (valueParser == null) {
                fallbackValueParser(WithSpan(key, keyStart..keyEnd), separator)
            }
            else {
                val value = valueParser(separator)
                map[key] = value
            }

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
            else if (peek().type == TokenType.INVALID) {
                val t = peek()
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "リストの読み取りに失敗しました: 無効なトークンです: ${t.value}")
            }
            else {
                val t = peek()
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "マップオブジェクトの読み取りに失敗しました: 不明なトークンです: $t")
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

    sealed class ReadResult<T>(val successful: Boolean, val sourceSpan: SourceSpan) {
        class Successful<T>(val value: T, sourceSpan: SourceSpan): ReadResult<T>(successful = true, sourceSpan)

        /**
         * つまり fatal
         */
        class Failure<T>(sourceSpan: SourceSpan) : ReadResult<T>(successful = false, sourceSpan)
    }

    /**
     * unfatalizable = true の exception のみを unfatal にする魔法
     * なお unfatalizable = true を指定する場所は選ばないとバグりかねないよ
     */
    fun <T : Any> unfatalize(diagnostics: MutableList<BclDiagnostic>, callback: TokenSeqReader.() -> T): ReadResult<T> {
        val start = getCurrentTokenStartPosition()

        val value = try { callback() }
        catch (error: BclTokenSeqReadException) {
            if (error.unfatalizable) {
                diagnostics.add(diagnostic(error.sourceSpan, error.description))
                null
            }
            else {
                throw error
            }
        }

        var end = getPreviousTokenEndPosition()

        if (start.index > end.index) {
            end = start
        }

        return if (value == null) ReadResult.Failure(start..end)
        else ReadResult.Successful(value, start..end)
    }

    /**
     * TODO allowFloat = false とかにしたとき非致命例外にしたいな...
     */
    fun readCommandString(allowFloat: Boolean = false, allowSingleSignOnly: Boolean = false, nonTerminators: CharArray = charArrayOf()): String {
        if (isOver()) {
            throw exception(getOverNextSourceSpan(), "文字列の解析に失敗しました: 入力が不足しています")
        }

        val start = getCurrentTokenStartPosition()

        val startIndex = peek().sourceSpan.start.index

        val firstChar = sourceCode.getOrNull(startIndex)

        val secondChar = sourceCode.getOrNull(startIndex + 1)

        var index = startIndex

        when (firstChar) {
            KeyChar.COLON -> {
                next()
                val end = getPreviousTokenEndPosition()
                throw exception(start..end, "文字列の解析に失敗しました: 文字列は '${KeyChar.COLON}' で開始できません")
            }
            KeyChar.WILDCARD -> {
                next()
                return firstChar.toString()
            }
            in setOf(KeyChar.PLUS, KeyChar.MINUS) -> {
                if (secondChar == null || secondChar in KeyChar.COMMAND_WORD_TERMINATORS) {
                    next()

                    if (allowSingleSignOnly) {
                        return firstChar.toString()
                    }

                    val end = getPreviousTokenEndPosition()
                    throw exception(
                        start..end,
                        "文字列の解析に失敗しました: 文字列は $firstChar で開始された場合直後にターミネータ文字または終端があってはなりません"
                    )
                }

                index++
            }
        }

        while (sourceCode[index] !in (KeyChar.COMMAND_WORD_TERMINATORS.filter { it !in nonTerminators })) {
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
            val pS = peek().sourceSpan
            throw exception(
                pS.start..pS.start,
                "文字列の解析に失敗しました: 文字列を文字 '$firstChar' で開始することはできません",
                unfatalizable = true
            )
        }
        else if (!allowFloat && string.matches("^\\d+(?:\\.\\d+)?$".toRegex())) {
            throw exception(
                start..end,
                "文字列の解析に失敗しました: 数値として構成される文字列は不正です",
                unfatalizable = true
            )
        }
        else if (string.matches("^\\.$".toRegex()) && string.length % 2 == 0) {
            throw exception(
                start..end,
                "文字列の解析に失敗しました: 文字列は偶数個の '${KeyChar.DOT}' に等しくあってはなりません",
                unfatalizable = true
            )
        }

        return string
    }

    fun readQuotableCommandString(allowFloat: Boolean = false): String {
        if (isOver()) {
            throw exception(getOverNextSourceSpan(), "二重引用符付き文字列または文字列の解析に失敗しました: 入力が不足しています")
        }

        return if (peek().type == TokenType.DOUBLE_QUOTED) {
            readQuotedString()
        }
        else {
            readCommandString(allowFloat)
        }
    }

    fun readJsonValue(semanticTokens: MutableList<BclSemanticToken>): CommandJsonValue<*> {
        val vStart = getCurrentTokenStartPosition()
        val t = peek()
        return when (t.type) {
            TokenType.WORD -> {
                next()
                val vEnd = getPreviousTokenEndPosition()
                when (t.value) {
                    "true" -> {
                        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.BOOLEAN, vStart..vEnd))
                        CommandJsonValue(true, vStart..vEnd)
                    }
                    "false" -> {
                        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.BOOLEAN, vStart..vEnd))
                        CommandJsonValue(false, vStart..vEnd)
                    }
                    "null" -> {
                        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.LITERAL, vStart..vEnd))
                        CommandJsonValue(null, vStart..vEnd)
                    }
                    else -> throw exception(vStart..vEnd, "json の読み取りに失敗しました: 不正な値です: ${t.value}")
                }
            }
            TokenType.INT -> {
                next()
                val vEnd = getPreviousTokenEndPosition()
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, vStart..vEnd))
                CommandJsonValue(t.value.toInt(), vStart..vEnd)
            }
            TokenType.FLOAT -> {
                next()
                val vEnd = getPreviousTokenEndPosition()
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, vStart..vEnd))
                CommandJsonValue(t.value.toFloat(), vStart..vEnd)
            }
            TokenType.DOUBLE_QUOTED -> {
                next()
                val vEnd = getPreviousTokenEndPosition()
                semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STRING, vStart..vEnd))
                CommandJsonValue(t.value, vStart..vEnd)
            }
            TokenType.SYMBOL -> {
                if (t `is` Symbol.BRACE_START) {
                    readJsonObject(semanticTokens)
                }
                else if (t `is` Symbol.ARRAY_START) {
                    readJsonArray(semanticTokens)
                }
                else {
                    next()
                    val vEnd = getPreviousTokenEndPosition()
                    throw exception(vStart..vEnd, "json の読み取りに失敗しました: 不正な値です: ${t.value}")
                }
            }
            TokenType.INVALID -> {
                next()
                val vEnd = getPreviousTokenEndPosition()
                throw exception(vStart..vEnd, "json の読み取りに失敗しました: 無効なトークンです: ${t.value}")
            }
            else -> {
                next()
                val vEnd = getPreviousTokenEndPosition()
                throw exception(vStart..vEnd, "json の読み取りに失敗しました: 不正な値です: ${t.value}")
            }
        }
    }

    fun readJsonArray(semanticTokens: MutableList<BclSemanticToken>): CommandJsonArray {
        val list = mutableListOf<CommandJsonValue<*>>()

        val start = getCurrentTokenStartPosition()

        expect(Symbol.ARRAY_START)

        if (peek() `is` Symbol.ARRAY_END) {
            next()
            val end = getPreviousTokenEndPosition()
            return CommandJsonArray(list, start..end)
        }

        while (true) {
            val element = readJsonValue(semanticTokens)

            list.add(element)

            if (isOver()) {
                throw exception(getOverNextSourceSpan(), "json の読み取りに失敗しました: 入力が不足しています")
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

    fun readJsonObject(semanticTokens: MutableList<BclSemanticToken>): CommandJsonObject {
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
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.PARAMETER, kStart..kEnd))
            expect(Symbol.COLON)

            val value = readJsonValue(semanticTokens)

            entries.add(CommandJsonValue(key, kStart..kEnd) to value)

            if (isOver()) {
                throw exception(getOverNextSourceSpan(), "json の読み取りに失敗しました: 入力が不足しています")
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

    fun getEntireSourceSpan(): SourceSpan {
        if (tokens.isEmpty()) {
            val position = SourcePosition(1, 0, 0)
            return position..position
        }

        val start = tokens.first().sourceSpan.start
        val end = tokens.last().sourceSpan.end
        return start..end
    }
}
