package io.github.takenoko4096.bcl.parser

import io.github.takenoko4096.bcl.ast.AssignmentOperation
import io.github.takenoko4096.bcl.ast.BinaryOperation
import io.github.takenoko4096.bcl.ast.Block
import io.github.takenoko4096.bcl.ast.Label
import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.ast.UnaryOperation
import io.github.takenoko4096.bcl.ast.expression.AssignmentExpression
import io.github.takenoko4096.bcl.ast.expression.BinaryExpression
import io.github.takenoko4096.bcl.ast.expression.Expression
import io.github.takenoko4096.bcl.ast.expression.ReferenceExpression
import io.github.takenoko4096.bcl.ast.expression.IntExpression
import io.github.takenoko4096.bcl.ast.expression.CallExpression
import io.github.takenoko4096.bcl.ast.expression.ErrorExpression
import io.github.takenoko4096.bcl.ast.expression.UnaryExpression
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.ast.statement.BreakStatement
import io.github.takenoko4096.bcl.ast.statement.ContinueStatement
import io.github.takenoko4096.bcl.ast.statement.IfElseStatement
import io.github.takenoko4096.bcl.ast.statement.ReturnStatement
import io.github.takenoko4096.bcl.ast.statement.SetStatement
import io.github.takenoko4096.bcl.ast.statement.Statement
import io.github.takenoko4096.bcl.ast.statement.WhileStatement
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.dispatcher.CommandDispatcher
import io.github.takenoko4096.bcl.brigadier.CommandSender
import io.github.takenoko4096.bcl.reader.CommandLineReader
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticToken
import io.github.takenoko4096.bcl.server.BclSemanticTokenType
import io.github.takenoko4096.bcl.tokenizer.`!is`
import io.github.takenoko4096.bcl.tokenizer.SourcePosition
import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import io.github.takenoko4096.bcl.tokenizer.Symbol
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.bcl.tokenizer.TokenType

class BclParser(
    sourceCode: String,
    tokens: List<Token>,
    sourceSpan: SourceSpan?,
    overSourcePosition: SourcePosition,
    private val dispatcher: CommandDispatcher,
    private val commentCommandDispatcher: CommandDispatcher,
    private val sender: CommandSender
) : TokenSeqReader(sourceCode, tokens, sourceSpan, overSourcePosition, ::BclParseException) {

    private fun expression(): Expression {
        skipNewLine()
        val expr = assignment()
        skipNewLine()
        return expr
    }

    private fun assignment(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val leftStart = getCurrentTokenStartPosition()
        var node = logicalOr()
        val leftEnd = getPreviousTokenEndPosition()

        if (node !is ReferenceExpression) {
            diagnostics.add(diagnostic(
                leftStart..leftEnd,
                "代入式の左辺は変数参照またはフィールド参照である必要があります: ${node::class.simpleName}"
            ))
        }

        if (isOverIgnoreNewLine()) return node

        val operatorStart = getCurrentTokenStartPosition()

        val operation = if (testThenAdvance(predicate = { it `is` Symbol.PLUS_ASSIGN })) {
            AssignmentOperation.PLUS_ASSIGN
        }
        else if (testThenAdvance(predicate = { it `is` Symbol.MINUS_ASSIGN })) {
            AssignmentOperation.MINUS_ASSIGN
        }
        else if (testThenAdvance(predicate = { it `is` Symbol.MULTIPLY_ASSIGN })) {
            AssignmentOperation.MULTIPLY_ASSIGN
        }
        else if (testThenAdvance(predicate = { it `is` Symbol.DIVIDE_ASSIGN})) {
            AssignmentOperation.DIVIDE_ASSIGN
        }
        else if (testThenAdvance(predicate = { it `is` Symbol.MOD_ASSIGN })) {
            AssignmentOperation.MOD_ASSIGN
        }
        else if (testThenAdvance(predicate = { it `is` Symbol.ASSIGN })) {
            AssignmentOperation.ASSIGN
        }
        else {
            return node
        }

        val operatorEnd = getPreviousTokenEndPosition()

        semanticTokens.add(BclSemanticToken(
            BclSemanticTokenType.SYMBOL,
            operatorStart..operatorEnd
        ))

        val value = assignment()

        node = AssignmentExpression(
            leftStart..value.sourceSpan.end,
            diagnostics,
            semanticTokens,
            operation,
            node,
            value
        )

        return node
    }

    private fun logicalOr(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()

        var node = logicalAnd()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.LOGICAL_OR) {
            val operatorStart = getCurrentTokenStartPosition()
            nextIgnoreNewLine()
            val operatorEnd = getPreviousTokenEndPosition()

            semanticTokens.add(BclSemanticToken(
                BclSemanticTokenType.SYMBOL,
                operatorStart..operatorEnd
            ))

            val right = logicalAnd()

            val end = getPreviousTokenEndPosition()

            node = BinaryExpression(
                start..end,
                diagnostics,
                semanticTokens,
                BinaryOperation.LOGICAL_OR,
                node,
                right
            )
        }

        return node
    }

    private fun logicalAnd(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()
        var node = comparison()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.LOGICAL_AND) {
            val operatorStart = getCurrentTokenStartPosition()
            nextIgnoreNewLine()
            val operatorEnd = getPreviousTokenEndPosition()

            semanticTokens.add(BclSemanticToken(
                BclSemanticTokenType.SYMBOL,
                operatorStart..operatorEnd
            ))

            val right = comparison()

            val end = getPreviousTokenEndPosition()

            node = BinaryExpression(
                start..end,
                diagnostics,
                semanticTokens,
                BinaryOperation.LOGICAL_AND,
                node,
                right
            )
        }

        return node
    }

    private fun comparison(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()
        var node = bitOr()

        while (!isOverIgnoreNewLine()) {
            val operator = peekIgnoreNewLine()

            val operation = if (operator `is` Symbol.LOGICAL_EQUALS) BinaryOperation.LOGICAL_EQUALS
            else if (operator `is` Symbol.LOGICAL_NOT_EQUALS) BinaryOperation.LOGICAL_NOT_EQUALS
            else if (operator `is` Symbol.LOGICAL_LESS) BinaryOperation.LOGICAL_LESS
            else if (operator `is` Symbol.LOGICAL_MORE) BinaryOperation.LOGICAL_MORE
            else if (operator `is` Symbol.LOGICAL_LESS_OR_EQUALS) BinaryOperation.LOGICAL_LESS_OR_EQUALS
            else if (operator `is` Symbol.LOGICAL_MORE_OR_EQUALS) BinaryOperation.LOGICAL_MORE_OR_EQUALS
            else break

            semanticTokens.add(BclSemanticToken(
                BclSemanticTokenType.SYMBOL,
                operator.sourceSpan
            ))

            val right = bitOr()

            val end = getPreviousTokenEndPosition()

            node = BinaryExpression(
                start..end,
                diagnostics,
                semanticTokens,
                operation,
                node,
                right
            )
        }

        return node
    }

    private fun bitOr(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()
        var node = bitXor()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.BIT_OR) {
            val operator = peekIgnoreNewLine()
            nextIgnoreNewLine()
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, operator.sourceSpan))

            val right = bitXor()
            val end = getPreviousTokenEndPosition()
            node = BinaryExpression(
                start..end,
                diagnostics,
                semanticTokens,
                BinaryOperation.BIT_OR,
                node,
                right
            )
        }

        return node
    }

    private fun bitXor(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()
        var node = bitAnd()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.BIT_XOR) {
            val operator = peekIgnoreNewLine()
            nextIgnoreNewLine()
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, operator.sourceSpan))

            val right = bitAnd()
            val end = getPreviousTokenEndPosition()
            node = BinaryExpression(
                start..end,
                diagnostics,
                semanticTokens,
                BinaryOperation.BIT_XOR,
                node,
                right
            )
        }

        return node
    }

    private fun bitAnd(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()
        var node = bitShift()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.BIT_AND) {
            val operator = peekIgnoreNewLine()
            nextIgnoreNewLine()
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, operator.sourceSpan))

            val right = bitShift()
            val end = getPreviousTokenEndPosition()
            node = BinaryExpression(
                start..end,
                diagnostics,
                semanticTokens,
                BinaryOperation.BIT_AND,
                node,
                right
            )
        }

        return node
    }

    private fun bitShift(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()
        var node = arithmetic()

        while (!isOverIgnoreNewLine()) {
            val operator = peekIgnoreNewLine()

            val operation = if (operator `is` Symbol.BIT_SHIFT_LEFT) BinaryOperation.BIT_SHIFT_LEFT
            else if (operator `is` Symbol.BIT_SHIFT_RIGHT) BinaryOperation.BIT_SHIFT_RIGHT
            else if (operator `is` Symbol.BIT_SHIFT_RIGHT_UNSIGNED) BinaryOperation.BIT_SHIFT_RIGHT_UNSIGNED
            else break

            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, operator.sourceSpan))

            nextIgnoreNewLine()
            val right = arithmetic()
            val end = getPreviousTokenEndPosition()
            node = BinaryExpression(
                start..end,
                diagnostics,
                semanticTokens,
                operation,
                node,
                right
            )
        }

        return node
    }

    private fun arithmetic(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()
        var node = term()

        while (!isOverIgnoreNewLine()) {
            val operator = peekIgnoreNewLine()
            val operation = if (operator `is` Symbol.PLUS) BinaryOperation.PLUS
            else if (peekIgnoreNewLine() `is` Symbol.MINUS) BinaryOperation.MINUS
            else break

            nextIgnoreNewLine()
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, operator.sourceSpan))

            val right = term()
            val end = getPreviousTokenEndPosition()
            node = BinaryExpression(
                start..end,
                diagnostics,
                semanticTokens,
                operation,
                node,
                right
            )
        }

        return node
    }

    private fun term(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()
        var node = power()

        while (!isOverIgnoreNewLine()) {
            val operator = peekIgnoreNewLine()
            val operation = if (operator `is` Symbol.MULTIPLY) BinaryOperation.MULTIPLY
            else if (operator `is` Symbol.DIVIDE) BinaryOperation.DIVIDE
            else if (operator `is` Symbol.MOD) BinaryOperation.MOD
            else break

            nextIgnoreNewLine()
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, operator.sourceSpan))

            val right = power()
            val end = getPreviousTokenEndPosition()

            node = BinaryExpression(
                start..end,
                diagnostics,
                semanticTokens,
                operation,
                node,
                right
            )
        }

        return node
    }

    private fun power(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()
        var node = prefixUnary()

        if (isOverIgnoreNewLine()) return node

        while (peekIgnoreNewLine() `is` Symbol.POWER) {
            val operator = peekIgnoreNewLine()
            nextIgnoreNewLine()
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, operator.sourceSpan))

            val right = prefixUnary()
            val end = getPreviousTokenEndPosition()
            node = BinaryExpression(
                start..end,
                diagnostics,
                semanticTokens,
                BinaryOperation.POWER,
                node,
                right
            )
        }

        return node
    }

    private fun prefixUnary(): Expression {
        if (isOverIgnoreNewLine()) {
            return factor()
        }

        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()

        val operator = peekIgnoreNewLine()
        val operation = if (operator `is` Symbol.LOGICAL_NOT) UnaryOperation.LOGICAL_NOT
        else if (operator `is` Symbol.MINUS) UnaryOperation.MINUS
        else if (operator `is` Symbol.BIT_NOT) UnaryOperation.BIT_INVERSE
        else return factor()

        nextIgnoreNewLine()
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.SYMBOL, operator.sourceSpan))

        val target = factor()
        val end = getPreviousTokenEndPosition()

        return UnaryExpression(
            start..end,
            diagnostics,
            semanticTokens,
            operation,
            target
        )
    }

    /**
     * リテラル・ラムダ・変数参照・関数呼び出し
     */
    private fun factor(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        if (isOverIgnoreNewLine()) {
            diagnostics.add(diagnostic(getOverNextSourceSpan(), "入力が不足しています"))
            return ErrorExpression(getOverNextSourceSpan(), diagnostics, semanticTokens)
        }

        val token = peekIgnoreNewLine()

        return if (token `is` Symbol.PARENTHESIS_START) {
            group()
        }
        else if (token.type in TokenType.TOKEN_TYPES_INTEGER) {
            integer()
        }
        else if (token.type == TokenType.WORD) {
            identifier()
        }
        else {
            nextIgnoreNewLine()
            diagnostics.add(diagnostic(
                getOverNextSourceSpan(),
                "無効なトークンタイプです: ${token.type}"
            ))
            ErrorExpression(token.sourceSpan, diagnostics, semanticTokens)
        }
    }

    private fun identifier(): Expression {
        val start = getCurrentTokenStartPosition()
        val name = readIgnoreNewLine(TokenType.WORD)
        val end = getPreviousTokenEndPosition()

        if (!isOverIgnoreNewLine() && peekIgnoreNewLine() `is` Symbol.PARENTHESIS_START) {
            return call(name, start..end)
        }

        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.PARAMETER, start..end))

        return ReferenceExpression(
            start..end,
            diagnostics,
            semanticTokens,
            name
        )
    }

    private fun group(): Expression {
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val expr = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        return expr
    }

    private fun call(identifier: String, sourceSpan: SourceSpan): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = sourceSpan.start

        expectIgnoreNewLine(Symbol.PARENTHESIS_START)

        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.IDENTIFIER, sourceSpan))

        val arguments = mutableListOf<Expression>()

        while (!isOverIgnoreNewLine()) {
            arguments.add(expression())

            val token = peekIgnoreNewLine()
            if (token `is` Symbol.COMMA) {
                nextIgnoreNewLine()
            }
            else if (token `is` Symbol.PARENTHESIS_END) {
                nextIgnoreNewLine()
                break
            }
            else {
                nextIgnoreNewLine()
                diagnostics.add(diagnostic(token.sourceSpan, "括弧が閉じられていません: ${token.sourceSpan}"))
                break
            }
        }

        val end = getPreviousTokenEndPosition()

        return CallExpression(
            start..end,
            diagnostics,
            semanticTokens,
            identifier,
            arguments
        )
    }

    private fun integer(): Expression {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val token = peekIgnoreNewLine()

        val constant = when (token.type) {
            TokenType.INT_RAD2 -> token.value.toInt(2)
            TokenType.INT_RAD8 -> token.value.toInt(8)
            TokenType.INT -> token.value.toInt()
            TokenType.INT_RAD16 -> token.value.toInt(16)
            TokenType.UINT_RAD2 -> token.value.toUInt(2).toInt()
            TokenType.UINT_RAD8 -> token.value.toUInt(8).toInt()
            TokenType.UINT -> token.value.toUInt().toInt()
            TokenType.UINT_RAD16 -> token.value.toUInt(16).toInt()
            else -> {
                diagnostics.add(diagnostic(token.sourceSpan, "無効なトークンタイプです: ${token.type}"))
                nextIgnoreNewLine()
                return ErrorExpression(token.sourceSpan, diagnostics, semanticTokens)
            }
        }

        nextIgnoreNewLine()
        semanticTokens.add(BclSemanticToken(BclSemanticTokenType.NUMBER, token.sourceSpan))

        return IntExpression(
            token.sourceSpan,
            diagnostics,
            semanticTokens,
            constant
        )
    }

    private fun command(reader: CommandLineReader, dispatcher: CommandDispatcher/*macros: Map<String, String>*/): CommandLineParseResult {
        return try {
            dispatcher.parse(reader, sender)
        }
        catch (_: Throwable) {
            CommandLineParseResult.error(sender, reader, dispatcher)
        }
    }

    private fun statement(): Statement {
        if (isOverIgnoreNewLine()) {
            throw exception(
                getOverNextSourceSpan(),
                "ステートメントを読み取れませんでした: 入力が不足しています"
            )
        }

        val start = getCurrentTokenStartPosition()

        val token = peekIgnoreNewLine()
        return if (token `is` Keyword.IF) ifElse()
        else if (token `is` Keyword.SET) set()
        else if (token `is` Keyword.RETURN) `return`()
        else if (token `is` Keyword.CONTINUE) `continue`()
        else if (token `is` Keyword.BREAK) `break`()
        else if (token `is` Keyword.WHILE) `while`()
        else if (token `is` Keyword.DO) doWhile()
        else {
            nextIgnoreNewLine()
            val end = getPreviousTokenEndPosition()
            throw exception(
                start..end,
                "ステートメントを読み取れませんでした: ${peekIgnoreNewLine()} はステートメントとして無効なトークンです"
            )
        }
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
            val token = peek()
            next()
            throw exception(token.sourceSpan, "ステートメントの直後には区切りが必要です: $token")
        }
    }

    private fun ifElse(): IfElseStatement {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.IF, then = {
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STATEMENT, it))
        })

        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val cond = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)

        val thenBlock = if (peekIgnoreNewLine() `is` Symbol.BRACE_START) {
            block(requireBraces = true)
        }
        else {
            val nodeStart = getCurrentTokenStartPosition()
            val node = node()
            val nodeEnd = getPreviousTokenEndPosition()

            Block(
                sourceSpan = nodeStart..nodeEnd,
                diagnostics = emptyList(),
                nodes =listOf(node)
            )
        }

        val elseBlock = if (!isOverIgnoreNewLine() && peekIgnoreNewLine() `is` Keyword.ELSE) {
            val startOfElse = getCurrentTokenStartPosition()
            nextIgnoreNewLine()
            val endOfElse = getPreviousTokenEndPosition()
            semanticTokens.add(BclSemanticToken(BclSemanticTokenType.STATEMENT, startOfElse..endOfElse))

            if (peekIgnoreNewLine() `is` Symbol.BRACE_START) {
                block(true)
            }
            else {
                val startOfNode = getCurrentTokenStartPosition()
                val node = node()
                val endOfNode = getPreviousTokenEndPosition()

                Block(
                    sourceSpan = startOfNode..endOfNode,
                    diagnostics = emptyList(),
                    nodes = listOf(node)
                )
            }
        }
        else {
            null
        }

        val end = getPreviousTokenEndPosition()

        return IfElseStatement(
            start..end,
            diagnostics,
            semanticTokens,
            cond,
            thenBlock,
            elseBlock
        )
    }

    private fun set(): SetStatement {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

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

        val end = getPreviousTokenEndPosition()

        return SetStatement(
            start..end,
            diagnostics,
            semanticTokens,
            identifier,
            parameters,
            block(true)
        )
    }

    private fun `return`(): ReturnStatement {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()

        val start = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.RETURN)

        if (peekIgnoreNewLine() `is` Symbol.SEMICOLON) {
            val end = getPreviousTokenEndPosition()
            nextIgnoreNewLine()
            return ReturnStatement(
                start..end,
                diagnostics,
                semanticTokens,
                null
            )
        }
        else if (peek() `is` Symbol.NEWLINE) {
            val end = getPreviousTokenEndPosition()
            next()
            return ReturnStatement(
                start..end,
                diagnostics,
                semanticTokens,
                null
            )
        }
        else if (peek().type != TokenType.SYMBOL || peek().value in Symbol.getExpressionStartableSymbols().map(Symbol::value)) {
            val expr = expression()
            val end = getPreviousTokenEndPosition()
            if (peekIgnoreNewLine() `is` Symbol.SEMICOLON) nextIgnoreNewLine()
            return ReturnStatement(
                start..end,
                diagnostics,
                semanticTokens,
                expr
            )
        }

        val end = getPreviousTokenEndPosition()

        throw exception(start..end, "invalid return statement")
    }

    private fun `while`(): WhileStatement {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()
        val start = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.WHILE)
        val label = labelNullable()
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val condition = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        val end = getPreviousTokenEndPosition()

        return WhileStatement(
            start..end,
            diagnostics,
            semanticTokens,
            false,
            label,
            condition,
            block(true)
        )
    }

    private fun doWhile(): WhileStatement {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()
        val start = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.DO)
        val label = labelNullable()
        val block = block(true)
        expectIgnoreNewLine(Keyword.WHILE)
        expectIgnoreNewLine(Symbol.PARENTHESIS_START)
        val condition = expression()
        expectIgnoreNewLine(Symbol.PARENTHESIS_END)
        val end = getPreviousTokenEndPosition()
        requireSeparator()

        return WhileStatement(
            start..end,
            diagnostics,
            semanticTokens,
            true,
            label,
            condition,
            block
        )
    }

    private fun labelNullable(): Label? {
        if (peek() `is` Symbol.HASH) {
            val s = getCurrentTokenStartPosition()
            next()
            val name = read(TokenType.WORD)
            return Label(s..getPreviousTokenEndPosition(), emptyList(), name)
        }
        else {
            return null
        }
    }

    private fun `continue`(): ContinueStatement {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()
        val start = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.CONTINUE)
        val label = labelNullable()
        val end = getPreviousTokenEndPosition()
        requireSeparator()
        return ContinueStatement(
            start..end,
            diagnostics,
            semanticTokens,
            label
        )
    }

    private fun `break`(): BreakStatement {
        val diagnostics = mutableListOf<BclDiagnostic>()
        val semanticTokens = mutableListOf<BclSemanticToken>()
        val start = getCurrentTokenStartPosition()
        expectIgnoreNewLine(Keyword.BREAK)
        val label = labelNullable()
        val end = getPreviousTokenEndPosition()
        requireSeparator()
        return BreakStatement(
            start..end,
            diagnostics,
            semanticTokens,
            label
        )
    }

    private fun block(requireBraces: Boolean): Block {
        if (isOverIgnoreNewLine()) {
            throw exception(getOverNextSourceSpan(), "ブロックが期待されましたが、入力が不足しています")
        }

        val diagnostics = mutableListOf<BclDiagnostic>()

        val start = getCurrentTokenStartPosition()

        if (requireBraces) expectIgnoreNewLine(Symbol.BRACE_START)

        val nodes = mutableListOf<Node>()

        while (true) {
            if (isOverIgnoreNewLine()) {
                if (requireBraces) {
                    diagnostics.add(diagnostic(getOverNextSourceSpan(), "ブロックが閉じられていません"))
                }
                break
            }

            if (requireBraces && peekIgnoreNewLine() `is` Symbol.BRACE_END) {
                nextIgnoreNewLine()
                break
            }

            nodes.add(node())
        }

        val end = getPreviousTokenEndPosition()

        return Block(start..end, emptyList(), nodes)
    }

    private fun node(): Node {
        if (isOver()) {
            throw exception(getOverNextSourceSpan(), "命令のパースに失敗しました: 入力が不足しています")
        }

        // TODO beta
        val node = if (sender.beta && sender.extension && peekIgnoreNewLine().value.startsWith(Symbol.AT.value)) {
            statement()
        }
        else {
            val reader = CommandLineReader.of(this, sender)
            val dispatcher = if (reader.isCommentCommand) commentCommandDispatcher else dispatcher
            command(reader, dispatcher)
        }

        while (!isOverIgnoreNewLine() && peekIgnoreNewLine() `is` Symbol.SEMICOLON) {
            nextIgnoreNewLine()
        }

        return node
    }

    fun parse(): Block {
        if (isOverIgnoreNewLine()) {
            return Block(
                sourceSpan = getOverNextSourceSpan(),
                diagnostics = emptyList(),
                nodes = emptyList()
            )
        }

        return try {
            block(false)
        }
        finally {
            cursor = 0
        }
    }
}
