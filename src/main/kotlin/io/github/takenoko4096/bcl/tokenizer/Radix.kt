package io.github.takenoko4096.bcl.tokenizer

enum class Radix(val char: Char?, val value: Int) {
    BINARY('b', 2) {
        override fun apply(signed: Boolean) = if (signed) TokenType.INT_RAD2 else TokenType.UINT_RAD2
    },
    OCTAL('o', 8) {
        override fun apply(signed: Boolean) = if (signed) TokenType.INT_RAD8 else TokenType.UINT_RAD8
    },
    DECIMAL(null, 10) {
        override fun apply(signed: Boolean) = if (signed) TokenType.INT else TokenType.UINT
    },
    HEXADECIMAL('x', 16) {
        override fun apply(signed: Boolean) = if (signed) TokenType.INT_RAD16 else TokenType.UINT_RAD16
    };

    abstract fun apply(signed: Boolean): TokenType
}
