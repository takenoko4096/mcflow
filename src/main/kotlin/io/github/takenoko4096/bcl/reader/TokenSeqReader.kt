package io.github.takenoko4096.bcl.reader

import io.github.takenoko4096.bcl.brigadier.value.location.CoordinateComponent
import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier
import io.github.takenoko4096.bcl.parser.BclParseException
import io.github.takenoko4096.bcl.parser.Keyword
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType
import kotlin.collections.set

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
            throw exception(peekSafely(-1).sourceSpan.end, "整数が期待されましたが、入力が不足しています")
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

    fun readFloat(): Float {
        if (isOver()) {
            throw exception(peekSafely(-1).sourceSpan.end, "浮動小数点数が期待されましたが、入力が不足しています")
        }

        return read(TokenType.FLOAT, TokenType.INT).toFloat()
    }

    fun readQuotedString(): String {
        return read(TokenType.DOUBLE_QUOTED)
    }

    // TODO
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

    fun readNamespacedIdentifier(): NamespacedIdentifier {
        val start = getCurrentTokenStartPosition()
        val string = readCommandString()
        val end = getPreviousTokenEndPosition()

        val identifier = NamespacedIdentifier.parse(string)
            ?: run {
                throw exception(start..end, "識別子の読み取りに失敗しました: 文字列 '$string' は識別子として無効です (識別子は '${NamespacedIdentifier.REGEX}' の形式に従う必要があります)")
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
            val max = if (peek() `is` Symbol.RANGE) {
                next()
                val t = peek()
                if (t.type == TokenType.INT) {
                    next()
                    t.value.toInt()
                }
                else Int.MAX_VALUE
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

    fun readCommandString(allowFloat: Boolean = false): String {
        if (isOver()) {
            throw exception(peekSafely(-1).sourceSpan, "CommandWord の解析に失敗しました: 入力が不足しています")
        }

        val start = getCurrentTokenStartPosition()

        val startIndex = peek().sourceSpan.start.index

        val secondChar = sourceCode.getOrNull(startIndex + 1)

        if (sourceCode[startIndex] == KeyChar.COLON) {
            next()
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "CommandWord の解析に失敗しました: CommandWord は '${KeyChar.COLON}' で開始できません")
        }
        else if (sourceCode[startIndex] == KeyChar.MINUS && secondChar != null && secondChar in KeyChar.COMMAND_WORD_TERMINATORS) {
            next()
            val end = getPreviousTokenEndPosition()
            throw exception(start..end, "CommandWord の解析に失敗しました: CommandWord は '${KeyChar.MINUS}$secondChar' で開始できません")
        }

        var index = startIndex
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
            throw exception(peekSafely(0).sourceSpan, "CommandWord の解析に失敗しました: 無効なトークンです: ${peekSafely(0)}")
        }
        else if (!allowFloat && string.matches("^\\d+(?:\\.\\d+)?$".toRegex())) {
            throw exception(start..end, "CommandWord の解析に失敗しました: 数値として構成される CommandWord は不正です")
        }
        else if (string.matches("^\\.$".toRegex()) && string.length % 2 == 0) {
            throw exception(start..end, "CommandWord の解析に失敗しました: CommandWord は偶数個の '${KeyChar.DOT}' に等しくあってはなりません")
        }

        return string
    }

    fun readQuotableCommandString(): String {
        if (isOver()) {
            throw exception(peekSafely(-1).sourceSpan, "Quoted または CommandWord の解析に失敗しました: 入力が不足しています")
        }

        return if (peek().type == TokenType.DOUBLE_QUOTED) {
            readQuotedString()
        }
        else {
            readCommandString()
        }
    }
}
