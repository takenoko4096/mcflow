package io.github.takenoko4096.bcl.parser

import io.github.takenoko4096.bcl.ast.TypeAnnotation
import io.github.takenoko4096.bcl.ast.AssignmentOperation
import io.github.takenoko4096.bcl.ast.BinaryOperation
import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.ast.Label
import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.ast.Parameter
import io.github.takenoko4096.bcl.ast.Type
import io.github.takenoko4096.bcl.ast.UnaryOperation
import io.github.takenoko4096.bcl.ast.command.CommandLine
import io.github.takenoko4096.bcl.ast.command.node.CommandNode
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
import io.github.takenoko4096.bcl.ast.statement.SetStatement
import io.github.takenoko4096.bcl.ast.statement.VarStatement
import io.github.takenoko4096.bcl.ast.statement.Statement
import io.github.takenoko4096.bcl.ast.statement.WhileStatement
import io.github.takenoko4096.bcl.tokenizer.`!is`
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType
import io.github.takenoko4096.bcl.tokenizer.`is`

class BclParser(private val text: String, private val tokens: List<Token>) {
    private var cursor: Int = 0

    private fun isOver(): Boolean {
        return cursor >= tokens.size
    }

    private fun peek(): Token {
        if (isOver()) {
            throw exception(getCurrentTokenStartPosition(), "コード外に到達しました")
        }

        return tokens[cursor]
    }

    private fun peek(offset: Int): Token? {
        return tokens.getOrNull(cursor + offset)
    }

    private fun getClamped(offset: Int): Token {
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

    private fun next() {
        cursor++
    }

    private fun expect(symbol: Symbol) {
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
        val start = getCurrentTokenStartPosition()
        var i = 0
        var token: Token?
        do {
            token = peek(i) ?: throw exception(start, "コード外に到達しました")
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

    private fun expectIgnoreNewLine(keyword: Keyword) {
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

    private fun read(type: TokenType): String {
        val start = getCurrentTokenStartPosition()
        val current = peek()
        if (current.type == type) {
            next()
            return current.value
        }
        else {
            next()
            throw exception(start, "$type のトークンが期待されましたが、トークンの種類が一致しませんでした: ${current.type}")
        }
    }

    private fun readIgnoreNewLine(type: TokenType): String {
        val start = getCurrentTokenStartPosition()
        val current = peekIgnoreNewLine()
        if (current.type == type) {
            nextIgnoreNewLine()
            return current.value
        }
        else {
            nextIgnoreNewLine()
            throw exception(start, "$type のトークンが期待されましたが、トークンの種類が一致しませんでした: ${current.type}")
        }
    }

    private fun getCurrentTokenStartPosition(): SourcePosition {
        return getClamped(0).sourceSpan.start
    }

    private fun getPreviousTokenEndPosition(): SourcePosition {
        return getClamped(-1).sourceSpan.end
    }

    private fun exception(start: SourcePosition, message: String, cause: Throwable? = null): BclParseException {
        return BclParseException(message, text, start..getPreviousTokenEndPosition(), cause)
    }

    private fun expression(): Expression {
        skipNewLine()
        val expr = assignment()
        skipNewLine()
        return expr
    }

    private fun assignment(): Expression {
        val start = getCurrentTokenStartPosition()

        var node = logicalOr()

        if (isOverIgnoreNewLine()) return node

        fun throwIfLeftExprIsNotRef() {
            if (node !is VariableReferenceExpression) {
                throw exception(start, "代入式の左辺は変数参照またはフィールド参照である必要があります: ${node::class.simpleName}")
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
            node = AssignmentExpression(start..getPreviousTokenEndPosition(), operation, node, value)
        }

        return node
    }

    private fun logicalOr(): Expression {
        val s = getCurrentTokenStartPosition()

        var node = logicalAnd()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.LOGICAL_OR) {
            nextIgnoreNewLine()
            val right = logicalAnd()
            node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.LOGICAL_OR, node, right)
        }

        return node
    }

    private fun logicalAnd(): Expression {
        val s = getCurrentTokenStartPosition()
        var node = comparison()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.LOGICAL_AND) {
            nextIgnoreNewLine()
            val right = comparison()
            node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.LOGICAL_AND, node, right)
        }

        return node
    }

    private fun comparison(): Expression {
        val s = getCurrentTokenStartPosition()
        var node = bitOr()

        while (!isOverIgnoreNewLine()) {
            if (peekIgnoreNewLine() `is` Symbol.LOGICAL_EQUALS) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.LOGICAL_EQUALS, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_NOT_EQUALS) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.LOGICAL_NOT_EQUALS, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_LESS) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.LOGICAL_LESS, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_MORE) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.LOGICAL_MORE, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_LESS_OR_EQUALS) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.LOGICAL_LESS_OR_EQUALS, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.LOGICAL_MORE_OR_EQUALS) {
                nextIgnoreNewLine()
                val right = bitOr()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.LOGICAL_MORE_OR_EQUALS, node, right)
            }
            else {
                break
            }
        }

        return node
    }

    private fun bitOr(): Expression {
        val s = getCurrentTokenStartPosition()
        var node = bitXor()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.BIT_OR) {
            nextIgnoreNewLine()
            val right = bitXor()
            node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.BIT_OR, node, right)
        }

        return node
    }

    private fun bitXor(): Expression {
        val s = getCurrentTokenStartPosition()
        var node = bitAnd()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.BIT_XOR) {
            nextIgnoreNewLine()
            val right = bitAnd()
            node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.BIT_XOR, node, right)
        }

        return node
    }

    private fun bitAnd(): Expression {
        val s = getCurrentTokenStartPosition()
        var node = bitShift()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.BIT_AND) {
            nextIgnoreNewLine()
            val right = bitShift()
            node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.BIT_AND, node, right)
        }

        return node
    }

    private fun bitShift(): Expression {
        val s = getCurrentTokenStartPosition()
        var node = arithmetic()

        while (!isOverIgnoreNewLine()) {
            skipNewLine()
            if (peek(0) `is` Symbol.LOGICAL_LESS && peek(1) `is` Symbol.LOGICAL_LESS) {
                next(); next()
                skipNewLine()
                val right = arithmetic()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.BIT_SHIFT_LEFT, node, right)
            }
            else if (peek(0) `is` Symbol.LOGICAL_MORE && peek(1) `is` Symbol.LOGICAL_MORE && peek(2) `is` Symbol.LOGICAL_MORE) {
                next(); next(); next()
                skipNewLine()
                val right = arithmetic()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.BIT_SHIFT_RIGHT_UNSIGNED, node, right)
            }
            else if (peek(0) `is` Symbol.LOGICAL_MORE && peek(1) `is` Symbol.LOGICAL_MORE) {
                next(); next()
                skipNewLine()
                val right = arithmetic()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.BIT_SHIFT_RIGHT, node, right)
            }
            else break
        }

        return node
    }

    private fun arithmetic(): Expression {
        val s = getCurrentTokenStartPosition()
        var node = term()

        while (!isOverIgnoreNewLine()) {
            if (peekIgnoreNewLine() `is` Symbol.PLUS) {
                nextIgnoreNewLine()
                val right = term()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.PLUS, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.MINUS) {
                nextIgnoreNewLine()
                val right = term()
                node  = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.MINUS, node, right)
            }
            else {
                break
            }
        }

        return node
    }

    private fun term(): Expression {
        val s = getCurrentTokenStartPosition()
        var node = power()

        while (!isOverIgnoreNewLine()) {
            if (peekIgnoreNewLine() `is` Symbol.MULTIPLY) {
                nextIgnoreNewLine()
                val right = power()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.MULTIPLY, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.DIVIDE) {
                nextIgnoreNewLine()
                val right = power()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.DIVIDE, node, right)
            }
            else if (peekIgnoreNewLine() `is` Symbol.MOD) {
                nextIgnoreNewLine()
                val right = power()
                node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.MOD, node, right)
            }
            else {
                break
            }
        }

        return node
    }

    private fun power(): Expression {
        val s = getCurrentTokenStartPosition()
        var node = prefixUnary()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.POWER) {
            nextIgnoreNewLine()
            val right = prefixUnary()
            node = BinaryExpression(s..getPreviousTokenEndPosition(), BinaryOperation.POWER, node, right)
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

        val s = getCurrentTokenStartPosition()

        return if (peekIgnoreNewLine() `is` Symbol.LOGICAL_NOT) {
            nextIgnoreNewLine()
            val target = factor()
            UnaryExpression(s..getPreviousTokenEndPosition(), UnaryOperation.LOGICAL_NOT, target)
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_NOT) {
            nextIgnoreNewLine()
            val target = factor()
            UnaryExpression(s..getPreviousTokenEndPosition(), UnaryOperation.BIT_INVERSE, target)
        }
        else if (peekIgnoreNewLine() `is` Symbol.PLUS) {
            nextIgnoreNewLine()
            val target = factor()
            UnaryExpression(s..getPreviousTokenEndPosition(), UnaryOperation.PLUS, target)
        }
        else if (peekIgnoreNewLine() `is` Symbol.MINUS) {
            nextIgnoreNewLine()
            val target = factor()
            UnaryExpression(s..getPreviousTokenEndPosition(), UnaryOperation.MINUS, target)
        }
        else {
            factor()
        }
    }

    /**
     * リテラル・ラムダ・変数参照・関数呼び出し
     */
    private fun factor(): Expression {
        val start = getCurrentTokenStartPosition()

        if (isOverIgnoreNewLine()) {
            throw exception(start, "コード外に到達しました")
        }

        val current = peekIgnoreNewLine()

        return when (current.type) {
            TokenType.SYMBOL -> {
                if (current `is` Symbol.PARENTHESIS_START) group()
                else throw exception(start, "factorとして予期しないトークンです: $current")
            }
            TokenType.CHAR -> char()
            TokenType.STRING -> string()
            TokenType.WORD -> referenceOrInvocation()
            else -> number()
        }
    }

    private fun referenceOrInvocation(): Expression {
        val s = getCurrentTokenStartPosition()
        val access = readIgnoreNewLine(TokenType.WORD)

        return if (!isOverIgnoreNewLine() && (peekIgnoreNewLine() `is` Symbol.PARENTHESIS_START || peekIgnoreNewLine() `is` Symbol.BRACE_START)) {
            val args = arguments()
            FunctionCallExpression(s..getPreviousTokenEndPosition(), access, args)
        }
        else {
            VariableReferenceExpression(s..getPreviousTokenEndPosition(), access)
        }
    }

    private fun char(): CharExpression {
        val s = getCurrentTokenStartPosition()
        val c = readIgnoreNewLine(TokenType.CHAR)[0]
        return CharExpression(s..getPreviousTokenEndPosition(), c)
    }

    private fun string(): StringExpression {
        val s = getCurrentTokenStartPosition()
        val str = readIgnoreNewLine(TokenType.STRING)
        return StringExpression(s..getPreviousTokenEndPosition(), str)
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
        val start = getCurrentTokenStartPosition()

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
                throw exception(start, "実引数リストの読み取りに失敗しました: 無効なトークンです: ${peekIgnoreNewLine()}")
            }
        }

        throw exception(start, "関数呼び出しの引数の解析に失敗しました: 丸括弧が閉じられていません")
    }

    private fun number(): NumberExpression {
        val s = getCurrentTokenStartPosition()
        val current = peekIgnoreNewLine()

        val expr = when (current.type) {
            TokenType.INT_RAD2 -> {
                nextIgnoreNewLine()
                IntExpression(s..getPreviousTokenEndPosition(), current.value.toInt(2))
            }
            TokenType.INT_RAD8 -> {
                nextIgnoreNewLine()
                IntExpression(s..getPreviousTokenEndPosition(), current.value.toInt(8))
            }
            TokenType.INT -> {
                nextIgnoreNewLine()
                IntExpression(s..getPreviousTokenEndPosition(), current.value.toInt())
            }
            TokenType.INT_RAD16 -> {
                nextIgnoreNewLine()
                IntExpression(s..getPreviousTokenEndPosition(), current.value.toInt(16))
            }
            TokenType.UINT_RAD2 -> {
                nextIgnoreNewLine()
                UIntExpression(s..getPreviousTokenEndPosition(), current.value.toUInt(2))
            }
            TokenType.UINT_RAD8 -> {
                nextIgnoreNewLine()
                UIntExpression(s..getPreviousTokenEndPosition(), current.value.toUInt(8))
            }
            TokenType.UINT -> {
                nextIgnoreNewLine()
                UIntExpression(s..getPreviousTokenEndPosition(), current.value.toUInt())
            }
            TokenType.UINT_RAD16 -> {
                nextIgnoreNewLine()
                UIntExpression(s..getPreviousTokenEndPosition(), current.value.toUInt(16))
            }
            TokenType.FLOAT -> {
                nextIgnoreNewLine()
                FloatExpression(s..getPreviousTokenEndPosition(), current.value.toFloat())
            }
            TokenType.DOUBLE -> {
                nextIgnoreNewLine()
                DoubleExpression(s..getPreviousTokenEndPosition(), current.value.toDouble())
            }
            else -> {
                throw exception(s, "数値リテラルの解析に失敗しました: 不適切な種類のトークンです: ${current.type}")
            }
        }

        return expr
    }

    private fun typeAnnotation(): TypeAnnotation {
        val start = getCurrentTokenStartPosition()

        val t = peekIgnoreNewLine()

        if (t.type != TokenType.WORD) {
            throw exception(start, "型注釈の解釈に失敗しました: 予期しないトークンです ($t)")
        }

        for (type in Type.entries) {
            if (t.value == type.value) {
                return TypeAnnotation(type, start..getPreviousTokenEndPosition())
            }
        }

        throw exception(start, "型注釈の解釈に失敗しました: 不明な型です ($t)")
    }

    private fun command(): CommandLine? {
        if (peekIgnoreNewLine() `is` Symbol.DIVIDE) {
            nextIgnoreNewLine()
        }
        else {
            return null
        }

        val start = getCurrentTokenStartPosition()

        return CommandLine(start..getPreviousTokenEndPosition(), commandNodeTree())
    }

    private fun commandNodeTree(): CommandNode {

    }

    private fun selector() {
        val s = getCurrentTokenStartPosition()
    }

    private fun argument() {

    }

    private fun statement(): Statement? {
        if (isOverIgnoreNewLine()) {
            return null
        }

        if (peekIgnoreNewLine() `is` Symbol.AT) {
            nextIgnoreNewLine()
        }
        else {
            return null
        }

        return if (peekIgnoreNewLine() `is` Keyword.IF) ifElse()
        else if (peekIgnoreNewLine() `is` Keyword.VAR || peekIgnoreNewLine() `is` Keyword.CONST) `var`()
        else if (peekIgnoreNewLine() `is` Keyword.SET) set()
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

        val start = getCurrentTokenStartPosition()

        if (peekIgnoreNewLine() `is` Symbol.SEMICOLON) {
            nextIgnoreNewLine()
        }
        else if (peek() `is` Symbol.NEWLINE) {
            next()
        }
        else {
            val a = peek()
            next()
            throw exception(start, "ステートメントの直後には区切りが必要です: $a")
        }
    }

    private fun ifElse(): IfElseStatement {
        val s = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.IF)

        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val cond = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)

        val thenBlock = if (peekIgnoreNewLine() `is` Symbol.BRACE_START) {
            block(true)
        }
        else {
            val s2 = getCurrentTokenStartPosition()
            val node = node()
            Block(s2..getPreviousTokenEndPosition(), listOf(node))
        }

        if (!isOverIgnoreNewLine() && peekIgnoreNewLine() `is` Keyword.ELSE) {
            nextIgnoreNewLine()
            val elseBlock = if (peekIgnoreNewLine() `is` Symbol.BRACE_START) {
                block(true)
            }
            else {
                val s2 = getCurrentTokenStartPosition()
                val node = node()
                Block(s2..getPreviousTokenEndPosition(), listOf(node))
            }
            return IfElseStatement(s..getPreviousTokenEndPosition(), cond, thenBlock, elseBlock)
        }

        return IfElseStatement(s..getPreviousTokenEndPosition(), cond, thenBlock, null)
    }

    private fun `var`(): VarStatement {
        val s = getCurrentTokenStartPosition()

        val isReadonly = if (peekIgnoreNewLine() `is` Keyword.CONST) {
            nextIgnoreNewLine()
            true
        }
        else false

        expectIgnoreNewLine(Keyword.VAR)

        val identifier = readIgnoreNewLine(TokenType.WORD)

        val typeAnnotation: TypeAnnotation?
        if (peekIgnoreNewLine() `is` Symbol.COLON) {
            nextIgnoreNewLine()
            typeAnnotation = typeAnnotation()
        }
        else {
            typeAnnotation = null
        }

        expectIgnoreNewLine(Symbol.ASSIGN)

        val expr = expression()

        requireSeparator()

        return VarStatement(s..getPreviousTokenEndPosition(), isReadonly, typeAnnotation, identifier, expr)
    }

    private fun set(): SetStatement {
        val start = getCurrentTokenStartPosition()

        expectIgnoreNewLine(Keyword.SET)

        val identifier = readIgnoreNewLine(TokenType.WORD)
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)

        val parameters = mutableListOf<Parameter>()

        if (peekIgnoreNewLine() `!is` Symbol.PARENTHESIS_END) do {
            val name = readIgnoreNewLine(TokenType.WORD)
            val annot = typeAnnotation()
            parameters.add(Parameter(start..getPreviousTokenEndPosition(), name, annot))
        }
        while (peekIgnoreNewLine() `is` Symbol.COMMA)

        expectIgnoreNewLine(Symbol.PARENTHESIS_END)

        return SetStatement(start..getPreviousTokenEndPosition(), identifier, parameters, block(true))
    }

    private fun `return`(): ReturnStatement {
        val s = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.RETURN)

        if (peekIgnoreNewLine() `is` Symbol.SEMICOLON) {
            nextIgnoreNewLine()
            return ReturnStatement(s..getPreviousTokenEndPosition(), null)
        }
        else if (peek() `is` Symbol.NEWLINE) {
            next()
            return ReturnStatement(s..getPreviousTokenEndPosition(), null)
        }
        else if (canStartExpr(peek(), includeVaguePlusOrMinus = true)) {
            val expr = expression()
            if (peekIgnoreNewLine() `is` Symbol.SEMICOLON) nextIgnoreNewLine()
            return ReturnStatement(s..getPreviousTokenEndPosition(), expr)
        }
        else {
            return ReturnStatement(s..getPreviousTokenEndPosition(), null)
        }
    }

    private fun `for`(): ForStatement {
        val s = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.FOR)
        val label = labelNullable()
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val name = readIgnoreNewLine(TokenType.WORD)
        expectIgnoreNewLine(Keyword.IN)
        val iterable = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        val block = block(true)

        return ForStatement(s..getPreviousTokenEndPosition(), label, name, iterable, block)
    }

    private fun `while`(): WhileStatement {
        val s = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.WHILE)
        val label = labelNullable()
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val condition = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)

        return WhileStatement(s..getPreviousTokenEndPosition(), false, label, condition, block(true))
    }

    private fun doWhile(): WhileStatement {
        val s = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.DO)
        val label = labelNullable()
        val block = block(true)
        expectIgnoreNewLine(Keyword.WHILE)
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val condition = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        requireSeparator()

        return WhileStatement(s..getPreviousTokenEndPosition(), true, label, condition, block)
    }

    private fun labelNullable(): Label? {
        if (peek() `is` Symbol.HASH) {
            val s = getCurrentTokenStartPosition()
            next()
            val name = read(TokenType.WORD)
            return Label(s..getPreviousTokenEndPosition(), name)
        }
        else {
            return null
        }
    }

    private fun `continue`(): ContinueStatement {
        val s = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.CONTINUE)
        val label = labelNullable()
        requireSeparator()
        return ContinueStatement(s..getPreviousTokenEndPosition(), label)
    }

    private fun `break`(): BreakStatement {
        val s = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.BREAK)
        val label = labelNullable()
        requireSeparator()
        return BreakStatement(s..getPreviousTokenEndPosition(), label)
    }

    private fun block(requireBraces: Boolean): Block {
        val start = getCurrentTokenStartPosition()

        if (isOverIgnoreNewLine()) {
            throw exception(start, "ブロックが期待されましたが、コード外に到達しました")
        }

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

        return Block(start..getPreviousTokenEndPosition(), nodes)
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
