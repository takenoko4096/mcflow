package io.github.takenoko4096.bcl.tokenizer

enum class Radix(val char: Char?, val value: Int) {
    BINARY('b', 2) {
        override fun apply(original: TokenType) = when (original) {
            TokenType.INT -> TokenType.INT_RAD2
            TokenType.LONG -> TokenType.LONG_RAD2
            TokenType.UINT -> TokenType.UINT_RAD2
            TokenType.ULONG -> TokenType.ULONG_RAD2
            else -> throw IllegalArgumentException()
        }
    },
    OCTAL('o', 8) {
        override fun apply(original: TokenType) = when (original) {
            TokenType.INT -> TokenType.INT_RAD8
            TokenType.LONG -> TokenType.LONG_RAD8
            TokenType.UINT -> TokenType.UINT_RAD8
            TokenType.ULONG -> TokenType.ULONG_RAD8
            else -> throw IllegalArgumentException()
        }
    },
    DECIMAL(null, 10) {
        override fun apply(original: TokenType) = when (original) {
            TokenType.INT -> TokenType.INT
            TokenType.LONG -> TokenType.LONG
            TokenType.UINT -> TokenType.UINT
            TokenType.ULONG -> TokenType.ULONG
            TokenType.DECIMAL -> TokenType.DECIMAL
            TokenType.DECIMAL_POSTFIX_FLOAT -> TokenType.DECIMAL_POSTFIX_FLOAT
            TokenType.DECIMAL_POSTFIX_DOUBLE -> TokenType.DECIMAL_POSTFIX_DOUBLE
            else -> throw IllegalArgumentException(original.toString())
        }
    },
    HEXADECIMAL('x', 16) {
        override fun apply(original: TokenType) = when (original) {
            TokenType.INT -> TokenType.INT_RAD16
            TokenType.LONG -> TokenType.LONG_RAD16
            TokenType.UINT -> TokenType.UINT_RAD16
            TokenType.ULONG -> TokenType.ULONG_RAD16
            else -> throw IllegalArgumentException()
        }
    };

    abstract fun apply(original: TokenType): TokenType
}
