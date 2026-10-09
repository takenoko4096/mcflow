package io.github.takenoko4096.bcl.tokenizer

import io.github.takenoko4096.bcl.parser.Keyword

open class Token(
    val id: Int,
    val type: TokenType,
    open val value: String,
    val sourceSpan: SourceSpan
) {
    infix fun `is`(symbol: Symbol): Boolean {
        return type == TokenType.SYMBOL && value == symbol.value
    }

    infix fun `is`(keyword: Keyword): Boolean {
        return type == TokenType.WORD && value == keyword.value
    }

    override fun toString(): String {
        return "(${type.name}) [[ $value ]]"
    }

    class Invalid(id: Int, type: TokenType, val message: String, sourceSpan: SourceSpan) : Token(id, type, message, sourceSpan) {

        override fun toString(): String {
            return message
        }
    }
}
