package io.github.takenoko4096.bcl.parser

import io.github.takenoko4096.bcl.ast.BinaryOperation
import io.github.takenoko4096.bcl.ast.expression.BinaryExpression
import io.github.takenoko4096.bcl.ast.expression.DoubleExpression
import io.github.takenoko4096.bcl.ast.expression.Expression
import io.github.takenoko4096.bcl.ast.expression.FloatExpression
import io.github.takenoko4096.bcl.ast.expression.IntExpression
import io.github.takenoko4096.bcl.ast.expression.LongExpression
import io.github.takenoko4096.bcl.ast.expression.NumberExpression
import io.github.takenoko4096.bcl.ast.expression.UIntExpression
import io.github.takenoko4096.bcl.ast.expression.ULongExpression
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType
import io.github.takenoko4096.bcl.tokenizer.`is`
import io.github.takenoko4096.miasma.parse.node.UntypedBlock
import io.github.takenoko4096.miasma.parse.node.UntypedLabel
import io.github.takenoko4096.miasma.parse.node.UntypedNode
import io.github.takenoko4096.miasma.parse.node.expression.UntypedArrayExpression
import io.github.takenoko4096.miasma.parse.node.expression.UntypedAssignmentExpression
import io.github.takenoko4096.miasma.parse.node.expression.UntypedBinaryExpression
import io.github.takenoko4096.miasma.parse.node.expression.UntypedCharExpression
import io.github.takenoko4096.miasma.parse.node.expression.UntypedFunctionCallExpression
import io.github.takenoko4096.miasma.parse.node.expression.UntypedStringExpression
import io.github.takenoko4096.miasma.parse.node.expression.UntypedUnaryExpression
import io.github.takenoko4096.miasma.parse.node.expression.UntypedVariableReferenceExpression
import io.github.takenoko4096.miasma.parse.node.statement.UntypedBreakStatement
import io.github.takenoko4096.miasma.parse.node.statement.UntypedContinueStatement
import io.github.takenoko4096.miasma.parse.node.statement.UntypedFnStatement
import io.github.takenoko4096.miasma.parse.node.statement.UntypedForStatement
import io.github.takenoko4096.miasma.parse.node.statement.UntypedIfElseStatement
import io.github.takenoko4096.miasma.parse.node.statement.UntypedReturnStatement
import io.github.takenoko4096.miasma.parse.node.statement.UntypedStatement
import io.github.takenoko4096.miasma.parse.node.statement.UntypedVarStatement
import io.github.takenoko4096.miasma.parse.node.statement.UntypedWhileStatement

class BclParser(private val tokens: List<Token>) {
    private var cursor: Int = 0

    private fun isOver(): Boolean {
        return cursor >= tokens.size
    }

    private fun peek(): Token {
        if (isOver()) {
            throw exception("コード外に到達しました")
        }

        return tokens[cursor]
    }

    private fun peek(offset: Int): Token? {
        return tokens.getOrNull(cursor + offset)
    }

    private fun peekClamped(offset: Int): Token {
        return if (cursor + offset < 0) {
            tokens.first()
        }
        else if (cursor + offset >= tokens.size) {
            tokens.last()
        }
        else {
            tokens[cursor + offset]
        }
    }

    private fun getStartPosOnCurrentToken(): SourcePosition {
        return peekClamped(0).sourceSpan.start
    }

    private fun getEndPosOnPreviousToken(): SourcePosition {
        return peekClamped(-1).sourceSpan.end
    }

    private fun next() {
        cursor++
    }

    private fun expect(symbol: Symbol) {
        if (peek() `is` symbol) {
            next()
        }
        else {
            throw exception("予期しないトークンです: ${peek().value}")
        }
    }

    private fun isOverIgnoreNewLine(): Boolean {
        var i = 0
        var token: Token?
        do {
            token = peek(i) ?: return true
            i++
        }
        while (token `is` Symbol.NEWLINE)

        return false
    }

    private fun peekIgnoreNewLine(): Token {
        var i = 0
        var token: Token?
        do {
            token = peek(i) ?: throw exception("コード外に到達しました")
            i++
        }
        while (token `is` Symbol.NEWLINE)

        return token
    }

    private fun peekIgnoreNewLine(offset: Int): Token? {
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

    private fun skipNewLine() {
        while (!isOver() && peek() `is` Symbol.NEWLINE) {
            next()
        }
    }

    private fun nextIgnoreNewLine() {
        skipNewLine()
        next()
    }

    private fun expectIgnoreNewLine(symbol: Symbol) {
        if (peekIgnoreNewLine() `is` symbol) {
            nextIgnoreNewLine()
        }
        else {
            throw exception("${symbol.value} が期待されましたが、予期しないトークンが見つかりました: ${peekIgnoreNewLine().value}")
        }
    }

    private fun expectIgnoreNewLine(keyword: Keyword) {
        if (isOverIgnoreNewLine()) {
            throw exception("期待されたトークン ${keyword.value} に対してコード外への到達が返されました")
        }

        if (peekIgnoreNewLine() `is` keyword) {
            nextIgnoreNewLine()
        }
        else {
            throw exception("期待されたトークン ${keyword.value} に対して予期しないトークンです: ${peekIgnoreNewLine().value}")
        }
    }

    private fun read(type: TokenType): String {
        val current = peek()
        if (current.type == type) {
            next()
            return current.value
        }
        else {
            throw exception("$type のトークンが期待されましたが、トークンの種類が一致しませんでした: ${current.type}")
        }
    }

    private fun readIgnoreNewLine(type: TokenType): String {
        val current = peekIgnoreNewLine()
        if (current.type == type) {
            nextIgnoreNewLine()
            return current.value
        }
        else {
            throw exception("$type のトークンが期待されましたが、トークンの種類が一致しませんでした: ${current.type}")
        }
    }

    private fun exception(message: String, cause: Throwable? = null): BclTokenSequenceParseException {
        return BclTokenSequenceParseException(message, tokens, tokens.getOrNull(cursor) ?: tokens.last(), cursor, cause)
    }

    private fun expression(): Expression {
        skipNewLine()
        val expr = assignment()
        skipNewLine()
        return expr
    }

    private fun assignment(): Expression {
        var node = ternary()

        if (isOverIgnoreNewLine()) return node

        fun throwIfLeftExprIsNotRef() {
            if (node !is UntypedVariableReferenceExpression) {
                throw exception("代入式の左辺は変数参照またはフィールド参照である必要があります: ${node::class.simpleName}")
            }
        }

        val operation = if (peekIgnoreNewLine() `is` Symbol.PLUS_ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.PLUS_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.MINUS_ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.MINUS_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.MULTIPLY_ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.MULTIPLY_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.DIVIDE_ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.DIVIDE_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.MOD_ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.MOD_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.POWER_ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.POWER_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_AND_ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.BIT_AND_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_OR_ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.BIT_OR_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_XOR) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.BIT_XOR_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_SHIFT_LEFT_ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.BIT_SHIFT_LEFT_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_SHIFT_RIGHT_ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.BIT_SHIFT_RIGHT_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_SHIFT_RIGHT_UNSIGNED_ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.BIT_SHIFT_RIGHT_UNSIGNED_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.ASSIGN) {
            throwIfLeftExprIsNotRef()
            nextIgnoreNewLine()
            AssignmentOperation.ASSIGN
        }
        else null

        if (operation != null) {
            node = UntypedAssignmentExpression(operation, node, assignment())
        }

        return node
    }

    private fun logicalOr(): Expression {
        var node = logicalAnd()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.LOGICAL_OR) {
            nextIgnoreNewLine()
            node = UntypedBinaryExpression(BinaryOperation.LOGICAL_OR, node, logicalAnd())
        }

        return node
    }

    private fun logicalAnd(): Expression {
        start()
        var node = comparison()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.LOGICAL_AND) {
            nextIgnoreNewLine()
            node = BinaryExpression(end(), BinaryOperation.LOGICAL_AND, node, comparison())
        }

        return node
    }

    private fun comparison(): Expression {
        start()
        var node = bitOr()

        while (!isOverIgnoreNewLine()) {
            if (peekIgnoreNewLine() `is` Symbol.LOGICAL_EQUALS) {
                nextIgnoreNewLine()
                node = BinaryExpression(end(), BinaryOperation.LOGICAL_EQUALS, node, bitOr())
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_NOT_EQUALS) {
                nextIgnoreNewLine()
                node = BinaryExpression(end(), BinaryOperation.LOGICAL_NOT_EQUALS, node, bitOr())
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_LESS) {
                nextIgnoreNewLine()
                node = BinaryExpression(end(), BinaryOperation.LOGICAL_LESS, node, bitOr())
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_MORE) {
                nextIgnoreNewLine()
                node = BinaryExpression(end(), BinaryOperation.LOGICAL_MORE, node, bitOr())
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_LESS_OR_EQUALS) {
                nextIgnoreNewLine()
                node = BinaryExpression(end(), BinaryOperation.LOGICAL_LESS_OR_EQUALS, node, bitOr())
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_MORE_OR_EQUALS) {
                nextIgnoreNewLine()
                node = BinaryExpression(end(), BinaryOperation.LOGICAL_MORE_OR_EQUALS, node, bitOr())
            }
            else {
                break
            }
        }

        return node
    }

    private fun bitOr(): Expression {
        val s = getStartPosOnCurrentToken()
        var node = bitXor()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.BIT_OR) {
            nextIgnoreNewLine()
            val n = bitXor()
            node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.BIT_OR, node, n)
        }

        return node
    }

    private fun bitXor(): Expression {
        val s = getStartPosOnCurrentToken()
        var node = bitAnd()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.BIT_XOR) {
            nextIgnoreNewLine()
            val n = bitAnd()
            node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.BIT_XOR, node, n)
        }

        return node
    }

    private fun bitAnd(): Expression {
        val s = getStartPosOnCurrentToken()
        var node = bitShift()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.BIT_AND) {
            nextIgnoreNewLine()
            val n = bitShift()
            node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.BIT_AND, node, n)
        }

        return node
    }

    private fun bitShift(): Expression {
        var node = arithmetic()

        while (!isOverIgnoreNewLine()) {
            skipNewLine()
            if (peek(0) `is` Symbol.LOGICAL_LESS && peek(1) `is` Symbol.LOGICAL_LESS) {
                next(); next()
                skipNewLine()
                node = UntypedBinaryExpression(BinaryOperation.BIT_SHIFT_LEFT, node, arithmetic())
            }
            else if (peek(0) `is` Symbol.LOGICAL_MORE && peek(1) `is` Symbol.LOGICAL_MORE && peek(2) `is` Symbol.LOGICAL_MORE) {
                next(); next(); next()
                skipNewLine()
                node = UntypedBinaryExpression(BinaryOperation.BIT_SHIFT_RIGHT_UNSIGNED, node, arithmetic())
            }
            else if (peek(0) `is` Symbol.LOGICAL_MORE && peek(1) `is` Symbol.LOGICAL_MORE) {
                next(); next()
                skipNewLine()
                node = UntypedBinaryExpression(BinaryOperation.BIT_SHIFT_RIGHT, node, arithmetic())
            }
            else break
        }

        return node
    }

    private fun arithmetic(): Expression {
        var node = term()

        while (!isOverIgnoreNewLine()) {
            if (peekIgnoreNewLine() `is` Symbol.PLUS) {
                nextIgnoreNewLine()
                node = UntypedBinaryExpression(BinaryOperation.PLUS, node, term())
            }
            else if (peekIgnoreNewLine() `is` Symbol.MINUS) {
                nextIgnoreNewLine()
                node  = UntypedBinaryExpression(BinaryOperation.MINUS, node, term())
            }
            else {
                break
            }
        }

        return node
    }

    private fun term(): Expression {
        var node = power()

        while (!isOverIgnoreNewLine()) {
            if (peekIgnoreNewLine() `is` Symbol.MULTIPLY) {
                nextIgnoreNewLine()
                node = UntypedBinaryExpression(BinaryOperation.MULTIPLY, node, power())
            }
            else if (peekIgnoreNewLine() `is` Symbol.DIVIDE) {
                nextIgnoreNewLine()
                node = UntypedBinaryExpression(BinaryOperation.DIVIDE, node, power())
            }
            else if (peekIgnoreNewLine() `is` Symbol.MOD) {
                nextIgnoreNewLine()
                node = UntypedBinaryExpression(BinaryOperation.MOD, node, power())
            }
            else {
                break
            }
        }

        return node
    }

    private fun power(): Expression {
        var node = singleArgumentFunctionCall()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.POWER) {
            nextIgnoreNewLine()
            val right = singleArgumentFunctionCall()
            node = UntypedBinaryExpression(BinaryOperation.POWER, node, right)
        }

        return node
    }

    private fun canStartExpr(t: Token?, includeVaguePlusOrMinus: Boolean = false): Boolean {
        if (t == null) return false
        if (t.type != TokenType.SYMBOL) return true
        return t.value in (setOf(
            Symbol.PARENTHESIS_START,
            Symbol.BRACE_START,
            Symbol.ARRAY_START,
            Symbol.LOGICAL_NOT,
            Symbol.BIT_NOT
        ) + if (includeVaguePlusOrMinus) setOf(Symbol.PLUS, Symbol.MINUS) else setOf()).map(Symbol::value)
    }

    private fun prefixUnary(): Expression {
        if (isOverIgnoreNewLine()) {
            return prefixIncrementDecrement()
        }

        return if (peekIgnoreNewLine() `is` Symbol.LOGICAL_NOT) {
            nextIgnoreNewLine()
            UntypedUnaryExpression(UnaryOperation.LOGICAL_NOT, prefixIncrementDecrement())
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_NOT) {
            nextIgnoreNewLine()
            UntypedUnaryExpression(UnaryOperation.BIT_INVERSE, prefixIncrementDecrement())
        }
        else if (peekIgnoreNewLine() `is` Symbol.PLUS) {
            nextIgnoreNewLine()
            UntypedUnaryExpression(UnaryOperation.PLUS, prefixIncrementDecrement())
        }
        else if (peekIgnoreNewLine() `is` Symbol.MINUS) {
            nextIgnoreNewLine()
            UntypedUnaryExpression(UnaryOperation.MINUS, prefixIncrementDecrement())
        }
        else {
            prefixIncrementDecrement()
        }
    }

    /**
     * リテラル・ラムダ・変数参照・関数呼び出し
     */
    private fun factor(): Expression {
        if (isOverIgnoreNewLine()) {
            throw exception("コード外に到達しました")
        }

        val current = peekIgnoreNewLine()

        return when (current.type) {
            TokenType.SYMBOL -> {
                if (current `is` Symbol.PARENTHESIS_START) group()
                else if (current `is` Symbol.BRACE_START) lambda()
                else if (current `is` Symbol.ARRAY_START) array()
                else throw exception("factorとして予期しないトークンです: $current")
            }
            TokenType.CHAR -> char()
            TokenType.STRING -> string()
            TokenType.WORD -> {
                val access = namespacedAccess()

                if (!isOverIgnoreNewLine() && (peekIgnoreNewLine() `is` Symbol.PARENTHESIS_START || peekIgnoreNewLine() `is` Symbol.BRACE_START)) {
                    UntypedFunctionCallExpression(access, arguments())
                }
                else {
                    UntypedVariableReferenceExpression(access)
                }
            }
            else -> number()
        }
    }

    private fun char(): UntypedCharExpression {
        return UntypedCharExpression(readIgnoreNewLine(TokenType.CHAR)[0])
    }

    private fun string(): UntypedStringExpression {
        return UntypedStringExpression(readIgnoreNewLine(TokenType.STRING))
    }

    /**
     * グループ化された式を予測
     */
    private fun group(): Expression {
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val expr = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        return expr
    }

    private fun arguments(): List<Expression> {
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)

        val args = mutableListOf<Expression>()

        while (!isOverIgnoreNewLine()) {
            if (peekIgnoreNewLine() `is` Symbol.PARENTHESIS_END) {
                nextIgnoreNewLine()
                return args
            }

            args.add(expression())

            if (peekIgnoreNewLine() `is` Symbol.COMMA) {
                nextIgnoreNewLine()
            }
            else if (peekIgnoreNewLine() `is` Symbol.PARENTHESIS_END) {
                nextIgnoreNewLine()
                return args
            }
            else {
                throw exception("実引数リストの読み取りに失敗しました: 無効なトークンです: ${peekIgnoreNewLine()}")
            }
        }

        throw exception("関数呼び出しの引数の解析に失敗しました: 丸括弧が閉じられていません")
    }

    private fun number(): NumberExpression {
        val current = peekIgnoreNewLine()

        val expr = when (current.type) {
            TokenType.INT_RAD2 -> IntExpression(current.value.toInt(2))
            TokenType.INT_RAD8 -> IntExpression(current.value.toInt(8))
            TokenType.INT -> IntExpression(current.value.toInt())
            TokenType.INT_RAD16 -> IntExpression(current.value.toInt(16))
            TokenType.UINT_RAD2 -> UIntExpression(current.value.toUInt(2))
            TokenType.UINT_RAD8 -> UIntExpression(current.value.toUInt(8))
            TokenType.UINT -> UIntExpression(current.value.toUInt())
            TokenType.UINT_RAD16 -> UIntExpression(current.value.toUInt(16))
            TokenType.LONG_RAD2 -> LongExpression(current.value.toLong(2))
            TokenType.LONG_RAD8 -> LongExpression(current.value.toLong(8))
            TokenType.LONG -> LongExpression(current.value.toLong())
            TokenType.LONG_RAD16 -> LongExpression(current.value.toLong(16))
            TokenType.ULONG_RAD2 -> ULongExpression(current.value.toULong(2))
            TokenType.ULONG_RAD8 -> ULongExpression(current.value.toULong(8))
            TokenType.ULONG -> ULongExpression(current.value.toULong())
            TokenType.ULONG_RAD16 -> ULongExpression(current.value.toULong(16))
            TokenType.FLOAT -> FloatExpression(current.value.toFloat())
            TokenType.DOUBLE -> DoubleExpression(current.value.toDouble())
            else -> {
                throw exception("数値リテラルの解析に失敗しました: 不適切な種類のトークンです: ${current.type}")
            }
        }

        nextIgnoreNewLine()

        return expr
    }

    private fun array(): UntypedArrayExpression {
        expectIgnoreNewLine(Symbol.ARRAY_START)

        val elements = mutableListOf<UntypedExpression>()

        if (peekIgnoreNewLine() `is` Symbol.ARRAY_END) {
            nextIgnoreNewLine()
            return UntypedArrayExpression(elements)
        }

        while (!isOverIgnoreNewLine()) {
            elements.add(expression())

            if (peekIgnoreNewLine() `is` Symbol.COMMA) {
                nextIgnoreNewLine()
            }
            else if (peekIgnoreNewLine() `is` Symbol.ARRAY_END) {
                nextIgnoreNewLine()
                return UntypedArrayExpression(elements)
            }
            else {
                throw exception("配列リテラルの解析中に無効なトークンを検出しました: ${peekIgnoreNewLine()}")
            }
        }

        throw exception("配列リテラルの解析に失敗しました: 角括弧が閉じられていません")
    }

    private fun statement(): UntypedStatement? {
        return if (peekIgnoreNewLine() `is` Keyword.IF) ifElse()
        else if (peekIgnoreNewLine() `is` Keyword.VAR || peekIgnoreNewLine() `is` Keyword.CONST) `var`(AccessModifier.PUBLIC)
        else if (peekIgnoreNewLine() `is` Keyword.FN) fn(AccessModifier.PUBLIC)
        else if (peekIgnoreNewLine() `is` Keyword.RETURN) `return`()
        else if (peekIgnoreNewLine() `is` Keyword.FOR) `for`()
        else if (peekIgnoreNewLine() `is` Keyword.CONTINUE) `continue`()
        else if (peekIgnoreNewLine() `is` Keyword.BREAK) `break`()
        else if (peekIgnoreNewLine() `is` Keyword.WHILE) `while`()
        else if (peekIgnoreNewLine() `is` Keyword.DO) doWhile()
        else null
    }

    private fun requireSeparator() {
        if (isOverIgnoreNewLine()) return

        if (peekIgnoreNewLine() `is` Symbol.SEMICOLON) {
            nextIgnoreNewLine()
        }
        else if (peek() `is` Symbol.NEWLINE) {
            next()
        }
        else {
            throw exception("ステートメントの直後には区切りが必要です")
        }
    }

    private fun ifElse(): UntypedIfElseStatement {
        expectIgnoreNewLine(Keyword.IF)

        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val cond = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)

        val thenBlock = if (peekIgnoreNewLine() `is` Symbol.BRACE_START) {
            block(true)
        }
        else UntypedBlock(listOf(node()))

        if (!isOverIgnoreNewLine() && peekIgnoreNewLine() `is` Keyword.ELSE) {
            nextIgnoreNewLine()
            val elseBlock = if (peekIgnoreNewLine() `is` Symbol.BRACE_START) {
                block(true)
            }
            else UntypedBlock(listOf(node()))
            return UntypedIfElseStatement(cond, thenBlock, elseBlock)
        }

        return UntypedIfElseStatement(cond, thenBlock, null)
    }

    private fun `var`(accessModifier: AccessModifier): UntypedVarStatement {
        val isReadonly = if (peekIgnoreNewLine() `is` Keyword.VAR) {
            nextIgnoreNewLine()
            false
        }
        else if (peekIgnoreNewLine() `is` Keyword.CONST) {
            nextIgnoreNewLine()
            true
        }
        else throw exception("NEVER HAPPENS")

        val identifier = readIgnoreNewLine(TokenType.WORD)

        val typeAnnotation = if (peekIgnoreNewLine() `is` Symbol.COLON) {
            nextIgnoreNewLine()
            typeAnnotation()
        }
        else null

        val initialValue = if (peekIgnoreNewLine() `is` Symbol.ASSIGN) {
            nextIgnoreNewLine()
            expression()
        }
        else null

        requireSeparator()

        if (typeAnnotation == null && initialValue == null) {
            throw exception("型が推論不可能な変数宣言は無効です: $identifier")
        }

        return UntypedVarStatement(accessModifier, isReadonly, identifier, typeAnnotation, initialValue)
    }

    private fun fn(accessModifier: AccessModifier): UntypedFnStatement {
        expectIgnoreNewLine(Keyword.FN)

        val templates = if (peekIgnoreNewLine() `is` Symbol.LOGICAL_LESS) typeTemplates() else listOf()

        val identifier = readIgnoreNewLine(TokenType.WORD)
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)

        val parameters = mutableListOf<UntypedFnStatement.UntypedFunctionParameter>()

        if (peekIgnoreNewLine() `!is` Symbol.PARENTHESIS_END) do {
            val name = readIgnoreNewLine(TokenType.WORD)
            expectIgnoreNewLine(Symbol.COLON)
            val typeAnnotation = typeAnnotation()
            parameters.add(UntypedFnStatement.UntypedFunctionParameter(name, typeAnnotation))
        }
        while (peekIgnoreNewLine() `is` Symbol.COMMA)

        expectIgnoreNewLine(Symbol.PARENTHESIS_END)

        val returnTypeAnnotation = if (peekIgnoreNewLine() `is` Symbol.COLON) {
            nextIgnoreNewLine()
            typeAnnotation()
        }
        else null

        return UntypedFnStatement(accessModifier, templates, identifier, parameters, returnTypeAnnotation, block(true))
    }

    private fun `return`(): UntypedReturnStatement {
        expectIgnoreNewLine(Keyword.RETURN)

        if (peekIgnoreNewLine() `is` Symbol.SEMICOLON) {
            nextIgnoreNewLine()
            return UntypedReturnStatement(null)
        }
        else if (peek() `is` Symbol.NEWLINE) {
            next()
            return UntypedReturnStatement(null)
        }
        else if (canStartExpr(peek(), includeVaguePlusOrMinus = true)) {
            val expr = expression()
            if (peekIgnoreNewLine() `is` Symbol.SEMICOLON) nextIgnoreNewLine()
            return UntypedReturnStatement(expr)
        }
        else {
            return UntypedReturnStatement(null)
        }
    }

    private fun `for`(): UntypedForStatement {
        expectIgnoreNewLine(Keyword.FOR)
        val label = labelOrNone()
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val name = readIgnoreNewLine(TokenType.WORD)
        expectIgnoreNewLine(Keyword.IN)
        val iterable = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        val block = block(true)

        return UntypedForStatement(label, name, iterable, block)
    }

    private fun `while`(): UntypedWhileStatement {
        expectIgnoreNewLine(Keyword.WHILE)
        val label = labelOrNone()
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val condition = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)

        return UntypedWhileStatement(false, label, condition, block(true))
    }

    private fun doWhile(): UntypedWhileStatement {
        expectIgnoreNewLine(Keyword.DO)
        val label = labelOrNone()
        val block = block(true)
        expectIgnoreNewLine(Keyword.WHILE)
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val condition = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        requireSeparator()

        return UntypedWhileStatement(true, label, condition, block)
    }

    private fun labelOrNone(): UntypedLabel? {
        if (peek() `is` Symbol.HASH) {
            next()
            val name = read(TokenType.WORD)
            return UntypedLabel(name)
        }
        else {
            return null
        }
    }

    private fun `continue`(): UntypedContinueStatement {
        expectIgnoreNewLine(Keyword.CONTINUE)
        val label = labelOrNone()
        requireSeparator()
        return UntypedContinueStatement(label)
    }

    private fun `break`(): UntypedBreakStatement {
        expectIgnoreNewLine(Keyword.BREAK)
        val label = labelOrNone()
        requireSeparator()
        return UntypedBreakStatement(label)
    }

    private fun block(requireBraces: Boolean): UntypedBlock {
        if (isOverIgnoreNewLine()) {
            throw exception("ブロックが期待されましたが、コード外に到達しました")
        }

        if (requireBraces) expectIgnoreNewLine(Symbol.BRACE_START)

        val nodes = mutableListOf<UntypedNode>()

        while (true) {
            if (requireBraces && peekIgnoreNewLine() `is` Symbol.BRACE_END) {
                nextIgnoreNewLine()
                break
            }
            else if (isOverIgnoreNewLine()) {
                break
            }

            nodes.add(node())
        }

        return UntypedBlock(nodes)
    }

    private fun node(): UntypedNode {
        val node = statement() ?: expression()

        while (!isOverIgnoreNewLine() && peekIgnoreNewLine() `is` Symbol.SEMICOLON) {
            nextIgnoreNewLine()
        }

        return node
    }

    fun parse(): UntypedBlock {
        try {
            return block(false)
        }
        finally {
            cursor = 0
        }
    }
}
