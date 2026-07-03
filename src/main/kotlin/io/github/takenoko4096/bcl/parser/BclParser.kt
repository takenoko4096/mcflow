package io.github.takenoko4096.bcl.parser

import io.github.takenoko4096.bcl.ast.AssignmentOperation
import io.github.takenoko4096.bcl.ast.BinaryOperation
import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.ast.Label
import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.ast.Type
import io.github.takenoko4096.bcl.ast.UnaryOperation
import io.github.takenoko4096.bcl.ast.expression.AssignmentExpression
import io.github.takenoko4096.bcl.ast.expression.BinaryExpression
import io.github.takenoko4096.bcl.ast.expression.CharExpression
import io.github.takenoko4096.bcl.ast.expression.DoubleExpression
import io.github.takenoko4096.bcl.ast.expression.Expression
import io.github.takenoko4096.bcl.ast.expression.FloatExpression
import io.github.takenoko4096.bcl.ast.expression.FunctionCallExpression
import io.github.takenoko4096.bcl.ast.expression.IntExpression
import io.github.takenoko4096.bcl.ast.expression.NumberExpression
import io.github.takenoko4096.bcl.ast.expression.StringExpression
import io.github.takenoko4096.bcl.ast.expression.UIntExpression
import io.github.takenoko4096.bcl.ast.expression.UnaryExpression
import io.github.takenoko4096.bcl.ast.expression.VariableReferenceExpression
import io.github.takenoko4096.bcl.ast.statement.BreakStatement
import io.github.takenoko4096.bcl.ast.statement.ContinueStatement
import io.github.takenoko4096.bcl.ast.statement.ForStatement
import io.github.takenoko4096.bcl.ast.statement.IfElseStatement
import io.github.takenoko4096.bcl.ast.statement.ReturnStatement
import io.github.takenoko4096.bcl.ast.statement.VarStatement
import io.github.takenoko4096.bcl.ast.statement.Statement
import io.github.takenoko4096.bcl.ast.statement.WhileStatement
import io.github.takenoko4096.bcl.tokenizer.`!is`
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType
import io.github.takenoko4096.bcl.tokenizer.`is`

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
        val s = getStartPosOnCurrentToken()

        var node = logicalOr()

        if (isOverIgnoreNewLine()) return node

        fun throwIfLeftExprIsNotRef() {
            if (node !is VariableReferenceExpression) {
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
            val value = assignment()
            node = AssignmentExpression(s..getEndPosOnPreviousToken(), operation, node, value)
        }

        return node
    }

    private fun logicalOr(): Expression {
        val s = getStartPosOnCurrentToken()

        var node = logicalAnd()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.LOGICAL_OR) {
            nextIgnoreNewLine()
            val right = logicalAnd()
            node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.LOGICAL_OR, node, right)
        }

        return node
    }

    private fun logicalAnd(): Expression {
        val s = getStartPosOnCurrentToken()
        var node = comparison()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.LOGICAL_AND) {
            nextIgnoreNewLine()
            val right = comparison()
            node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.LOGICAL_AND, node, right)
        }

        return node
    }

    private fun comparison(): Expression {
        val s = getStartPosOnCurrentToken()
        var node = bitOr()

        while (!isOverIgnoreNewLine()) {
            if (peekIgnoreNewLine() `is` Symbol.LOGICAL_EQUALS) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.LOGICAL_EQUALS, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_NOT_EQUALS) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.LOGICAL_NOT_EQUALS, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_LESS) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.LOGICAL_LESS, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_MORE) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.LOGICAL_MORE, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_LESS_OR_EQUALS) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.LOGICAL_LESS_OR_EQUALS, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_MORE_OR_EQUALS) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.LOGICAL_MORE_OR_EQUALS, node, right)
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
            val right = bitXor()
            node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.BIT_OR, node, right)
        }

        return node
    }

    private fun bitXor(): Expression {
        val s = getStartPosOnCurrentToken()
        var node = bitAnd()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.BIT_XOR) {
            nextIgnoreNewLine()
            val right = bitAnd()
            node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.BIT_XOR, node, right)
        }

        return node
    }

    private fun bitAnd(): Expression {
        val s = getStartPosOnCurrentToken()
        var node = bitShift()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.BIT_AND) {
            nextIgnoreNewLine()
            val right = bitShift()
            node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.BIT_AND, node, right)
        }

        return node
    }

    private fun bitShift(): Expression {
        val s = getStartPosOnCurrentToken()
        var node = arithmetic()

        while (!isOverIgnoreNewLine()) {
            skipNewLine()
            if (peek(0) `is` Symbol.LOGICAL_LESS && peek(1) `is` Symbol.LOGICAL_LESS) {
                next(); next()
                skipNewLine()
                val right = arithmetic()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.BIT_SHIFT_LEFT, node, right)
            }
            else if (peek(0) `is` Symbol.LOGICAL_MORE && peek(1) `is` Symbol.LOGICAL_MORE && peek(2) `is` Symbol.LOGICAL_MORE) {
                next(); next(); next()
                skipNewLine()
                val right = arithmetic()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.BIT_SHIFT_RIGHT_UNSIGNED, node, right)
            }
            else if (peek(0) `is` Symbol.LOGICAL_MORE && peek(1) `is` Symbol.LOGICAL_MORE) {
                next(); next()
                skipNewLine()
                val right = arithmetic()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.BIT_SHIFT_RIGHT, node, right)
            }
            else break
        }

        return node
    }

    private fun arithmetic(): Expression {
        val s = getStartPosOnCurrentToken()
        var node = term()

        while (!isOverIgnoreNewLine()) {
            if (peekIgnoreNewLine() `is` Symbol.PLUS) {
                nextIgnoreNewLine()
                val right = term()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.PLUS, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.MINUS) {
                nextIgnoreNewLine()
                val right = term()
                node  = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.MINUS, node, right)
            }
            else {
                break
            }
        }

        return node
    }

    private fun term(): Expression {
        val s = getStartPosOnCurrentToken()
        var node = power()

        while (!isOverIgnoreNewLine()) {
            if (peekIgnoreNewLine() `is` Symbol.MULTIPLY) {
                nextIgnoreNewLine()
                val right = power()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.MULTIPLY, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.DIVIDE) {
                nextIgnoreNewLine()
                val right = power()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.DIVIDE, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.MOD) {
                nextIgnoreNewLine()
                val right = power()
                node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.MOD, node, right)
            }
            else {
                break
            }
        }

        return node
    }

    private fun power(): Expression {
        val s = getStartPosOnCurrentToken()
        var node = prefixUnary()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.POWER) {
            nextIgnoreNewLine()
            val right = prefixUnary()
            node = BinaryExpression(s..getEndPosOnPreviousToken(), BinaryOperation.POWER, node, right)
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
            return factor()
        }

        val s = getStartPosOnCurrentToken()

        return if (peekIgnoreNewLine() `is` Symbol.LOGICAL_NOT) {
            nextIgnoreNewLine()
            val target = factor()
            UnaryExpression(s..getEndPosOnPreviousToken(), UnaryOperation.LOGICAL_NOT, target)
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_NOT) {
            nextIgnoreNewLine()
            val target = factor()
            UnaryExpression(s..getEndPosOnPreviousToken(), UnaryOperation.BIT_INVERSE, target)
        }
        else if (peekIgnoreNewLine() `is` Symbol.PLUS) {
            nextIgnoreNewLine()
            val target = factor()
            UnaryExpression(s..getEndPosOnPreviousToken(), UnaryOperation.PLUS, target)
        }
        else if (peekIgnoreNewLine() `is` Symbol.MINUS) {
            nextIgnoreNewLine()
            val target = factor()
            UnaryExpression(s..getEndPosOnPreviousToken(), UnaryOperation.MINUS, target)
        }
        else {
            factor()
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
                else throw exception("factorとして予期しないトークンです: $current")
            }
            TokenType.CHAR -> char()
            TokenType.STRING -> string()
            TokenType.WORD -> referenceOrInvocation()
            else -> number()
        }
    }

    private fun referenceOrInvocation(): Expression {
        val s = getStartPosOnCurrentToken()
        val access = readIgnoreNewLine(TokenType.WORD)

        return if (!isOverIgnoreNewLine() && (peekIgnoreNewLine() `is` Symbol.PARENTHESIS_START || peekIgnoreNewLine() `is` Symbol.BRACE_START)) {
            val args = arguments()
            FunctionCallExpression(s..getEndPosOnPreviousToken(), access, args)
        }
        else {
            VariableReferenceExpression(s..getEndPosOnPreviousToken(), access)
        }
    }

    private fun char(): CharExpression {
        val s = getStartPosOnCurrentToken()
        val c = readIgnoreNewLine(TokenType.CHAR)[0]
        return CharExpression(s..getEndPosOnPreviousToken(), c)
    }

    private fun string(): StringExpression {
        val s = getStartPosOnCurrentToken()
        val str = readIgnoreNewLine(TokenType.STRING)
        return StringExpression(s..getEndPosOnPreviousToken(), str)
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
        val s = getStartPosOnCurrentToken()
        val current = peekIgnoreNewLine()

        val expr = when (current.type) {
            TokenType.INT_RAD2 -> {
                nextIgnoreNewLine()
                IntExpression(s..getEndPosOnPreviousToken(), current.value.toInt(2))
            }
            TokenType.INT_RAD8 -> {
                nextIgnoreNewLine()
                IntExpression(s..getEndPosOnPreviousToken(), current.value.toInt(8))
            }
            TokenType.INT -> {
                nextIgnoreNewLine()
                IntExpression(s..getEndPosOnPreviousToken(), current.value.toInt())
            }
            TokenType.INT_RAD16 -> {
                nextIgnoreNewLine()
                IntExpression(s..getEndPosOnPreviousToken(), current.value.toInt(16))
            }
            TokenType.UINT_RAD2 -> {
                nextIgnoreNewLine()
                UIntExpression(s..getEndPosOnPreviousToken(), current.value.toUInt(2))
            }
            TokenType.UINT_RAD8 -> {
                nextIgnoreNewLine()
                UIntExpression(s..getEndPosOnPreviousToken(), current.value.toUInt(8))
            }
            TokenType.UINT -> {
                nextIgnoreNewLine()
                UIntExpression(s..getEndPosOnPreviousToken(), current.value.toUInt())
            }
            TokenType.UINT_RAD16 -> {
                nextIgnoreNewLine()
                UIntExpression(s..getEndPosOnPreviousToken(), current.value.toUInt(16))
            }
            TokenType.FLOAT -> {
                nextIgnoreNewLine()
                FloatExpression(s..getEndPosOnPreviousToken(), current.value.toFloat())
            }
            TokenType.DOUBLE -> {
                nextIgnoreNewLine()
                DoubleExpression(s..getEndPosOnPreviousToken(), current.value.toDouble())
            }
            else -> {
                throw exception("数値リテラルの解析に失敗しました: 不適切な種類のトークンです: ${current.type}")
            }
        }

        return expr
    }

    private fun selector() {
        val s = getStartPosOnCurrentToken()

    }

    private fun type(): Type {

    }

    private fun statement(): Statement? {
        return if (peekIgnoreNewLine() `is` Keyword.IF) ifElse()
        else if (peekIgnoreNewLine() `is` Keyword.VAR || peekIgnoreNewLine() `is` Keyword.CONST) `var`()
        else if (peekIgnoreNewLine() `is` Keyword.MACRO) fn()
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

    private fun ifElse(): IfElseStatement {
        val s = getStartPosOnCurrentToken()
        expectIgnoreNewLine(Keyword.IF)

        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val cond = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)

        val thenBlock = if (peekIgnoreNewLine() `is` Symbol.BRACE_START) {
            block(true)
        }
        else {
            val s2 = getStartPosOnCurrentToken()
            val node = node()
            Block(s2..getEndPosOnPreviousToken(), listOf(node))
        }

        if (!isOverIgnoreNewLine() && peekIgnoreNewLine() `is` Keyword.ELSE) {
            nextIgnoreNewLine()
            val elseBlock = if (peekIgnoreNewLine() `is` Symbol.BRACE_START) {
                block(true)
            }
            else {
                val s2 = getStartPosOnCurrentToken()
                val node = node()
                Block(s2..getEndPosOnPreviousToken(), listOf(node))
            }
            return IfElseStatement(s..getEndPosOnPreviousToken(), cond, thenBlock, elseBlock)
        }

        return IfElseStatement(s..getEndPosOnPreviousToken(), cond, thenBlock, null)
    }

    private fun `var`(): VarStatement {
        val s = getStartPosOnCurrentToken()

        val isReadonly = if (peekIgnoreNewLine() `is` Keyword.CONST) {
            nextIgnoreNewLine()
            true
        }
        else false

        expectIgnoreNewLine(Keyword.VAR)

        val identifier = readIgnoreNewLine(TokenType.WORD)

        expectIgnoreNewLine(Symbol.ASSIGN)

        val expr = expression()

        requireSeparator()

        return VarStatement(s..getEndPosOnPreviousToken(), isReadonly, identifier, expr)
    }

    private fun fn(): FnStatement {
        expectIgnoreNewLine(Keyword.MACRO)

        val identifier = readIgnoreNewLine(TokenType.WORD)
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)

        val parameters = mutableListOf<FnStatement.UntypedFunctionParameter>()

        if (peekIgnoreNewLine() `!is` Symbol.PARENTHESIS_END) do {
            val name = readIgnoreNewLine(TokenType.WORD)
            parameters.add(FnStatement.UntypedFunctionParameter(name, typeAnnotation))
        }
        while (peekIgnoreNewLine() `is` Symbol.COMMA)

        expectIgnoreNewLine(Symbol.PARENTHESIS_END)

        return FnStatement(identifier, parameters, block(true))
    }

    private fun `return`(): ReturnStatement {
        val s = getStartPosOnCurrentToken()
        expectIgnoreNewLine(Keyword.RETURN)

        if (peekIgnoreNewLine() `is` Symbol.SEMICOLON) {
            nextIgnoreNewLine()
            return ReturnStatement(s..getEndPosOnPreviousToken(), null)
        }
        else if (peek() `is` Symbol.NEWLINE) {
            next()
            return ReturnStatement(s..getEndPosOnPreviousToken(), null)
        }
        else if (canStartExpr(peek(), includeVaguePlusOrMinus = true)) {
            val expr = expression()
            if (peekIgnoreNewLine() `is` Symbol.SEMICOLON) nextIgnoreNewLine()
            return ReturnStatement(s..getEndPosOnPreviousToken(), expr)
        }
        else {
            return ReturnStatement(s..getEndPosOnPreviousToken(), null)
        }
    }

    private fun `for`(): ForStatement {
        val s = getStartPosOnCurrentToken()
        expectIgnoreNewLine(Keyword.FOR)
        val label = labelNullable()
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val name = readIgnoreNewLine(TokenType.WORD)
        expectIgnoreNewLine(Keyword.IN)
        val iterable = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        val block = block(true)

        return ForStatement(s..getEndPosOnPreviousToken(), label, name, iterable, block)
    }

    private fun `while`(): WhileStatement {
        val s = getStartPosOnCurrentToken()
        expectIgnoreNewLine(Keyword.WHILE)
        val label = labelNullable()
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val condition = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)

        return WhileStatement(s..getEndPosOnPreviousToken(), false, label, condition, block(true))
    }

    private fun doWhile(): WhileStatement {
        val s = getStartPosOnCurrentToken()
        expectIgnoreNewLine(Keyword.DO)
        val label = labelNullable()
        val block = block(true)
        expectIgnoreNewLine(Keyword.WHILE)
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val condition = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        requireSeparator()

        return WhileStatement(s..getEndPosOnPreviousToken(), true, label, condition, block)
    }

    private fun labelNullable(): Label? {
        if (peek() `is` Symbol.HASH) {
            val s = getStartPosOnCurrentToken()
            next()
            val name = read(TokenType.WORD)
            return Label(s..getEndPosOnPreviousToken(), name)
        }
        else {
            return null
        }
    }

    private fun `continue`(): ContinueStatement {
        val s = getStartPosOnCurrentToken()
        expectIgnoreNewLine(Keyword.CONTINUE)
        val label = labelNullable()
        requireSeparator()
        return ContinueStatement(s..getEndPosOnPreviousToken(), label)
    }

    private fun `break`(): BreakStatement {
        val s = getStartPosOnCurrentToken()
        expectIgnoreNewLine(Keyword.BREAK)
        val label = labelNullable()
        requireSeparator()
        return BreakStatement(s..getEndPosOnPreviousToken(), label)
    }

    private fun block(requireBraces: Boolean): Block {
        if (isOverIgnoreNewLine()) {
            throw exception("ブロックが期待されましたが、コード外に到達しました")
        }

        val s = getStartPosOnCurrentToken()

        if (requireBraces) expectIgnoreNewLine(Symbol.BRACE_START)

        val nodes = mutableListOf<Node>()

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

        return Block(s..getEndPosOnPreviousToken(), nodes)
    }

    private fun node(): Node {
        val node = statement() ?: expression()

        while (!isOverIgnoreNewLine() && peekIgnoreNewLine() `is` Symbol.SEMICOLON) {
            nextIgnoreNewLine()
        }

        return node
    }

    fun parse(): Block {
        try {
            return block(false)
        }
        finally {
            cursor = 0
        }
    }
}
