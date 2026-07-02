package io.github.takenoko4096.bcl.tokenizer

import io.github.takenoko4096.bcl.parser.Keyword

data class Token(val type: TokenType, val value: String, val sourceSpan: SourceSpan) {
    infix fun `is`(symbol: Symbol): Boolean {
        return type == TokenType.SYMBOL && value == symbol.value
    }

    infix fun `is`(keyword: Keyword): Boolean {
        return type == TokenType.WORD && value == keyword.value
    }

    override fun toString(): String {
        return "($type) [[ $value ]]"
    }
}
