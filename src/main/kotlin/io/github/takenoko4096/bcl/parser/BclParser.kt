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
import io.github.takenoko4096.bcl.ast.argument.FloatArgument
import io.github.takenoko4096.bcl.ast.argument.IntArgument
import io.github.takenoko4096.bcl.ast.argument.NumberArgument
import io.github.takenoko4096.bcl.ast.argument.UIntArgument
import io.github.takenoko4096.bcl.ast.command.CommandLine
import io.github.takenoko4096.bcl.ast.expression.AssignmentExpression
import io.github.takenoko4096.bcl.ast.expression.BinaryExpression
import io.github.takenoko4096.bcl.ast.Calculatable
import io.github.takenoko4096.bcl.ast.argument.Argument
import io.github.takenoko4096.bcl.ast.expression.MacroCallExpression
import io.github.takenoko4096.bcl.ast.expression.UnaryExpression
import io.github.takenoko4096.bcl.ast.expression.VariableExpression
import io.github.takenoko4096.bcl.ast.statement.BreakStatement
import io.github.takenoko4096.bcl.ast.statement.ContinueStatement
import io.github.takenoko4096.bcl.ast.statement.ForStatement
import io.github.takenoko4096.bcl.ast.statement.IfElseStatement
import io.github.takenoko4096.bcl.ast.statement.ReturnStatement
import io.github.takenoko4096.bcl.ast.statement.SetStatement
import io.github.takenoko4096.bcl.ast.statement.VarStatement
import io.github.takenoko4096.bcl.ast.statement.Statement
import io.github.takenoko4096.bcl.ast.statement.WhileStatement
import io.github.takenoko4096.bcl.brigadier.CommandDispatcher
import io.github.takenoko4096.bcl.brigadier.node.ArgumentNode
import io.github.takenoko4096.bcl.brigadier.node.LiteralArgumentNode
import io.github.takenoko4096.bcl.brigadier.node.RequiredArgumentNode
import io.github.takenoko4096.bcl.tokenizer.`!is`
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType
import io.github.takenoko4096.bcl.tokenizer.`is`

class BclParser(private val text: String, private val tokens: List<Token>, private val dispatcher: CommandDispatcher) {
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

    private fun calculatable(): Calculatable {
        skipNewLine()
        val expr = assignment()
        skipNewLine()
        return expr
    }

    private fun assignment(): Calculatable {
        val start = getCurrentTokenStartPosition()

        var node = logicalOr()

        if (node !is VariableExpression) {
            throw exception(start, "代入式の左辺は変数参照またはフィールド参照である必要があります: ${node::class.simpleName}")
        }

        if (isOverIgnoreNewLine()) return node

        val operation = if (peekIgnoreNewLine() `is` Symbol.PLUS_ASSIGN) {
            nextIgnoreNewLine()
            AssignmentOperation.PLUS_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.MINUS_ASSIGN) {
            nextIgnoreNewLine()
            AssignmentOperation.MINUS_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.MULTIPLY_ASSIGN) {
            nextIgnoreNewLine()
            AssignmentOperation.MULTIPLY_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.DIVIDE_ASSIGN) {
            nextIgnoreNewLine()
            AssignmentOperation.DIVIDE_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.MOD_ASSIGN) {
            nextIgnoreNewLine()
            AssignmentOperation.MOD_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.POWER_ASSIGN) {
            nextIgnoreNewLine()
            AssignmentOperation.POWER_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_AND_ASSIGN) {
            nextIgnoreNewLine()
            AssignmentOperation.BIT_AND_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_OR_ASSIGN) {
            nextIgnoreNewLine()
            AssignmentOperation.BIT_OR_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_XOR) {
            nextIgnoreNewLine()
            AssignmentOperation.BIT_XOR_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_SHIFT_LEFT_ASSIGN) {
            nextIgnoreNewLine()
            AssignmentOperation.BIT_SHIFT_LEFT_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_SHIFT_RIGHT_ASSIGN) {
            nextIgnoreNewLine()
            AssignmentOperation.BIT_SHIFT_RIGHT_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.BIT_SHIFT_RIGHT_UNSIGNED_ASSIGN) {
            nextIgnoreNewLine()
            AssignmentOperation.BIT_SHIFT_RIGHT_UNSIGNED_ASSIGN
        }
        else if (peekIgnoreNewLine() `is` Symbol.ASSIGN) {
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

    private fun logicalOr(): Calculatable {
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

    private fun logicalAnd(): Calculatable {
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

    private fun comparison(): Calculatable {
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

    private fun bitOr(): Calculatable {
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

    private fun bitXor(): Calculatable {
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

    private fun bitAnd(): Calculatable {
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

    private fun bitShift(): Calculatable {
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

    private fun arithmetic(): Calculatable {
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

    private fun term(): Calculatable {
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

    private fun power(): Calculatable {
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

    private fun prefixUnary(): Calculatable {
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
    private fun factor(): Calculatable {
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
            TokenType.SINGLE_QUOTED, TokenType.DOUBLE_QUOTED -> throw exception(start, "unsupported token type")
            TokenType.WORD -> referenceOrMacroCall()
            else -> number()
        }
    }

    private fun referenceOrMacroCall(): Calculatable {
        val s = getCurrentTokenStartPosition()
        val access = readIgnoreNewLine(TokenType.WORD)

        return if (!isOverIgnoreNewLine() && (peekIgnoreNewLine() `is` Symbol.PARENTHESIS_START || peekIgnoreNewLine() `is` Symbol.BRACE_START)) {
            val args = macroArguments()
            MacroCallExpression(s..getPreviousTokenEndPosition(), access, args)
        }
        else {
            VariableExpression(s..getPreviousTokenEndPosition(), access)
        }
    }

    /**
     * グループ化された式を予測
     */
    private fun group(): Calculatable {
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val expr = calculatable()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        return expr
    }

    private fun macroArguments(): List<Calculatable> {
        val start = getCurrentTokenStartPosition()

        expectIgnoreNewLine(Symbol.PARENTHESIS_START)

        val args = mutableListOf<Calculatable>()

        while (!isOverIgnoreNewLine()) {
            if (peekIgnoreNewLine() `is` Symbol.PARENTHESIS_END) {
                nextIgnoreNewLine()
                return args
            }

            args.add(calculatable())

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

    private fun number(): NumberArgument {
        val s = getCurrentTokenStartPosition()
        val current = peekIgnoreNewLine()

        return when (current.type) {
            TokenType.INT_RAD2 -> {
                nextIgnoreNewLine()
                IntArgument(s..getPreviousTokenEndPosition(), current.value.toInt(2))
            }
            TokenType.INT_RAD8 -> {
                nextIgnoreNewLine()
                IntArgument(s..getPreviousTokenEndPosition(), current.value.toInt(8))
            }
            TokenType.INT -> {
                nextIgnoreNewLine()
                IntArgument(s..getPreviousTokenEndPosition(), current.value.toInt())
            }
            TokenType.INT_RAD16 -> {
                nextIgnoreNewLine()
                IntArgument(s..getPreviousTokenEndPosition(), current.value.toInt(16))
            }
            TokenType.UINT_RAD2 -> {
                nextIgnoreNewLine()
                UIntArgument(s..getPreviousTokenEndPosition(), current.value.toUInt(2))
            }
            TokenType.UINT_RAD8 -> {
                nextIgnoreNewLine()
                UIntArgument(s..getPreviousTokenEndPosition(), current.value.toUInt(8))
            }
            TokenType.UINT -> {
                nextIgnoreNewLine()
                UIntArgument(s..getPreviousTokenEndPosition(), current.value.toUInt())
            }
            TokenType.UINT_RAD16 -> {
                nextIgnoreNewLine()
                UIntArgument(s..getPreviousTokenEndPosition(), current.value.toUInt(16))
            }
            TokenType.FLOAT -> {
                nextIgnoreNewLine()
                FloatArgument(s..getPreviousTokenEndPosition(), current.value.toFloat())
            }
            else -> {
                throw exception(s, "数値リテラルの解析に失敗しました: 不明または未実装の種類のトークンです: ${current.type}")
            }
        }
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

    private fun command(): CommandLine {
        val start = getCurrentTokenStartPosition()

        if (peekIgnoreNewLine() `is` Symbol.DIVIDE) {
            nextIgnoreNewLine()
        }

        val name = read(TokenType.WORD)
        val command = dispatcher.getCommand(name) ?: throw exception(start, "不明なコマンド名です: $name")

        val arguments = mutableListOf<Argument>()

        var startOfArg: SourcePosition
        var current: Token
        var node: ArgumentNode? = command.root

        while (!isOverIgnoreNewLine()) {
            if (node == null) {
                break
            }

            startOfArg = getCurrentTokenStartPosition()
            current = peek(); next()

            if (current `is` Symbol.ESCAPE && peek() `is` Symbol.NEWLINE) {
                next()
            }

            if (current `is` Symbol.NEWLINE) {
                break
            }

            val arg = commandArgument(node, startOfArg)
            node = node.children
            arguments.add(arg)
        }

        return CommandLine(start..getPreviousTokenEndPosition(), name, arguments)
    }

    private fun commandArgument(previous: ArgumentNode, start: SourcePosition): Argument {
        when (previous) {
            is LiteralArgumentNode -> {

            }
            is RequiredArgumentNode -> {

            }
        }
    }

    private fun selector() {
        val start = getCurrentTokenStartPosition()
    }

    private fun statement(): Statement {
        val start = getCurrentTokenStartPosition()

        if (isOverIgnoreNewLine()) {
            throw exception(start, "ステートメントを読み取れませんでした: 解析中にコードの末尾に到達しました")
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
        else throw exception(start, "ステートメントを読み取れませんでした: ${peekIgnoreNewLine()} はステートメントとして無効なトークンです")
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
        val cond = calculatable()
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

        val expr = calculatable()

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
        else if (peek().type != TokenType.SYMBOL || peek().value in Symbol.getExpressionStartableSymbols().map(Symbol::value)) {
            val expr = calculatable()
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
        val iterable = calculatable()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        val block = block(true)

        return ForStatement(s..getPreviousTokenEndPosition(), label, name, iterable, block)
    }

    private fun `while`(): WhileStatement {
        val s = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.WHILE)
        val label = labelNullable()
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val condition = calculatable()
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
        val condition = calculatable()
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
        val node = if (peekIgnoreNewLine() `is` Symbol.AT) {
            nextIgnoreNewLine()
            statement()
        }
        else {
            command()
        }

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
