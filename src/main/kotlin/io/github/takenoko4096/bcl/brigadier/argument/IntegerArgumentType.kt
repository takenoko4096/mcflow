package io.github.takenoko4096.bcl.brigadier.argument

import io.github.takenoko4096.bcl.ast.Argument
import io.github.takenoko4096.bcl.ast.expression.IntExpression
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.tokenizer.TokenType

class IntegerArgumentType : ArgumentType<Int>("int") {
    override fun parse(reader: TokenSeqReader): Argument<Int> {
        val start = reader.getCurrentTokenStartPosition()
        val (_, type, value) = reader.peek()
        reader.next()
        val range = start..reader.getPreviousTokenEndPosition()

        val a = when (type) {
            TokenType.INT_RAD2 -> IntExpression(value.toInt(2), range)
            TokenType.INT_RAD8 -> IntExpression(value.toInt(8), range)
            TokenType.INT -> IntExpression(value.toInt(), range)
            TokenType.INT_RAD16 -> IntExpression(value.toInt(16), range)
            TokenType.UINT_RAD2 -> IntExpression(value.toUInt(2).toInt(), range)
            TokenType.UINT_RAD8 -> IntExpression(value.toUInt(8).toInt(), range)
            TokenType.UINT -> IntExpression(value.toUInt().toInt(), range)
            TokenType.UINT_RAD16 -> IntExpression(value.toUInt(16).toInt(), range)
            else -> throw reader.exception(start, "数値リテラルの解析に失敗しました: 不明または未実装の種類のトークンです: $type")
        }
    }
}
