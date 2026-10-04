package io.github.takenoko4096.bcl.parser

import io.github.takenoko4096.bcl.ast.AssignmentOperation
import io.github.takenoko4096.bcl.ast.BinaryOperation
import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.ast.Label
import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.ast.UnaryOperation
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.ast.expression.AssignmentExpression
import io.github.takenoko4096.bcl.ast.expression.BinaryExpression
import io.github.takenoko4096.bcl.ast.expression.Expression
import io.github.takenoko4096.bcl.ast.expression.IntExpression
import io.github.takenoko4096.bcl.ast.expression.MacroCallExpression
import io.github.takenoko4096.bcl.ast.expression.UnaryExpression
import io.github.takenoko4096.bcl.ast.expression.VariableExpression
import io.github.takenoko4096.bcl.ast.statement.BreakStatement
import io.github.takenoko4096.bcl.ast.statement.ContinueStatement
import io.github.takenoko4096.bcl.ast.statement.IfElseStatement
import io.github.takenoko4096.bcl.ast.statement.ReturnStatement
import io.github.takenoko4096.bcl.ast.statement.SetStatement
import io.github.takenoko4096.bcl.ast.statement.Statement
import io.github.takenoko4096.bcl.ast.statement.WhileStatement
import io.github.takenoko4096.bcl.brigadier.CommandDispatcher
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.`!is`
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType
import io.github.takenoko4096.bcl.tokenizer.`is`

class BclParser(
    sourceCode: String,
    tokens: List<Token>,
    private val dispatcher: CommandDispatcher,
    private val extension: Boolean
) : TokenSeqReader(sourceCode, tokens, ::BclParseException) {
    private fun expression(): Expression {
        skipNewLine()
        val expr = assignment()
        skipNewLine()
        return expr
    }

    private fun assignment(): Expression {
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
            TokenType.SINGLE_QUOTED, TokenType.DOUBLE_QUOTED -> throw exception(start, "unsupported token type")
            TokenType.WORD -> referenceOrMacroCall()
            else -> number()
        }
    }

    private fun referenceOrMacroCall(): Expression {
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
    private fun group(): Expression {
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val expr = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        return expr
    }

    private fun macroArguments(): List<Expression> {
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

    private fun number(): IntExpression {
        val s = getCurrentTokenStartPosition()
        val current = peekIgnoreNewLine()

        return when (current.type) {
            TokenType.INT_RAD2 -> {
                nextIgnoreNewLine()
                IntExpression(current.value.toInt(2), s..getPreviousTokenEndPosition())
            }
            TokenType.INT_RAD8 -> {
                nextIgnoreNewLine()
                IntExpression(current.value.toInt(8), s..getPreviousTokenEndPosition())
            }
            TokenType.INT -> {
                nextIgnoreNewLine()
                IntExpression(current.value.toInt(), s..getPreviousTokenEndPosition())
            }
            TokenType.INT_RAD16 -> {
                nextIgnoreNewLine()
                IntExpression(current.value.toInt(16), s..getPreviousTokenEndPosition())
            }
            TokenType.UINT_RAD2 -> {
                nextIgnoreNewLine()
                IntExpression(current.value.toUInt(2).toInt(), s..getPreviousTokenEndPosition())
            }
            TokenType.UINT_RAD8 -> {
                nextIgnoreNewLine()
                IntExpression(current.value.toUInt(8).toInt(), s..getPreviousTokenEndPosition())
            }
            TokenType.UINT -> {
                nextIgnoreNewLine()
                IntExpression(current.value.toUInt().toInt(), s..getPreviousTokenEndPosition())
            }
            TokenType.UINT_RAD16 -> {
                nextIgnoreNewLine()
                IntExpression(current.value.toUInt(16).toInt(), s..getPreviousTokenEndPosition())
            }
            else -> {
                throw exception(s, "数値リテラルの解析に失敗しました: 不明または未実装の種類のトークンです: ${current.type}")
            }
        }
    }

    private fun command(/*macros: Map<String, String>*/): CommandLineParseResult {
        return dispatcher.parse(createCommandLineSubReader())
    }

    private fun statement(): Statement {
        val start = getCurrentTokenStartPosition()

        if (isOverIgnoreNewLine()) {
            throw exception(start, "ステートメントを読み取れませんでした: 解析中にコードの末尾に到達しました")
        }

        return if (peekIgnoreNewLine() `is` Keyword.IF) ifElse()
        else if (peekIgnoreNewLine() `is` Keyword.SET) set()
        else if (peekIgnoreNewLine() `is` Keyword.RETURN) `return`()
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

    private fun set(): SetStatement {
        val start = getCurrentTokenStartPosition()

        expectIgnoreNewLine(Keyword.SET)

        val identifier = readIgnoreNewLine(TokenType.WORD)

        val parameters = mutableListOf<String>()

        if (peekIgnoreNewLine() `is` Symbol.PARENTHESIS_START) {
            nextIgnoreNewLine()

            if (peekIgnoreNewLine() `!is` Symbol.PARENTHESIS_END) do {
                val name = readIgnoreNewLine(TokenType.WORD)
                parameters.add(name)
            }
            while ((peekIgnoreNewLine() `is` Symbol.COMMA).also { if (it) nextIgnoreNewLine() })

            expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        }

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
            val expr = expression()
            if (peekIgnoreNewLine() `is` Symbol.SEMICOLON) nextIgnoreNewLine()
            return ReturnStatement(s..getPreviousTokenEndPosition(), expr)
        }
        else {
            return ReturnStatement(s..getPreviousTokenEndPosition(), null)
        }
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
        if (isOverIgnoreNewLine()) {
            throw exception(getOverNextSourceSpan(), "ブロックが期待されましたが、入力が不足しています")
        }

        val start = getCurrentTokenStartPosition()

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

        val end = getPreviousTokenEndPosition()

        return Block(start..end, nodes)
    }

    private fun node(): Node {
        if (isOver()) {
            throw exception(getOverNextSourceSpan(), "命令のパースに失敗しました: 入力が不足しています")
        }

        val node = if (extension && peekIgnoreNewLine().value.startsWith(Symbol.AT.value)) {
            statement()
        }
        else if (peekIgnoreNewLine() `is` Symbol.DIVIDE) {
            nextIgnoreNewLine()
            command()
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
        if (isOverIgnoreNewLine()) {
            return Block(SourceSpan.NULL, emptyList())
        }

        try {
            return block(false)
        }
        finally {
            cursor = 0
        }
    }
}
