package io.github.takenoko4096.bcl.tokenizer

enum class Radix(val char: Char?, val value: Int) {
    BINARY('b', 2) {
        override fun apply(original: TokenType) = when (original) {
            TokenType.BYTE -> TokenType.BYTE_RAD2
            TokenType.SHORT -> TokenType.SHORT_RAD2
            TokenType.INT -> TokenType.INT_RAD2
            TokenType.LONG -> TokenType.LONG_RAD2
            TokenType.UBYTE -> TokenType.UBYTE_RAD2
            TokenType.USHORT -> TokenType.USHORT_RAD2
            TokenType.UINT -> TokenType.UINT_RAD2
            TokenType.ULONG -> TokenType.ULONG_RAD2
            else -> throw IllegalArgumentException()
        }
    },
    OCTAL('o', 8) {
        override fun apply(original: TokenType) = when (original) {
            TokenType.BYTE -> TokenType.BYTE_RAD8
            TokenType.SHORT -> TokenType.SHORT_RAD8
            TokenType.INT -> TokenType.INT_RAD8
            TokenType.LONG -> TokenType.LONG_RAD8
            TokenType.UBYTE -> TokenType.UBYTE_RAD8
            TokenType.USHORT -> TokenType.USHORT_RAD8
            TokenType.UINT -> TokenType.UINT_RAD8
            TokenType.ULONG -> TokenType.ULONG_RAD8
            else -> throw IllegalArgumentException()
        }
    },
    DECIMAL(null, 10) {
        override fun apply(original: TokenType) = when (original) {
            TokenType.BYTE -> TokenType.BYTE
            TokenType.SHORT -> TokenType.SHORT
            TokenType.INT -> TokenType.INT
            TokenType.LONG -> TokenType.LONG
            TokenType.UBYTE -> TokenType.UBYTE
            TokenType.USHORT -> TokenType.USHORT
            TokenType.UINT -> TokenType.UINT
            TokenType.ULONG -> TokenType.ULONG
            TokenType.FLOAT -> TokenType.FLOAT
            TokenType.DOUBLE -> TokenType.DOUBLE
            else -> throw IllegalArgumentException(original.toString())
        }
    },
    HEXADECIMAL('x', 16) {
        override fun apply(original: TokenType) = when (original) {
            TokenType.BYTE -> TokenType.BYTE_RAD16
            TokenType.SHORT -> TokenType.SHORT_RAD16
            TokenType.INT -> TokenType.INT_RAD16
            TokenType.LONG -> TokenType.LONG_RAD16
            TokenType.UBYTE -> TokenType.UBYTE_RAD16
            TokenType.USHORT -> TokenType.USHORT_RAD16
            TokenType.UINT -> TokenType.UINT_RAD16
            TokenType.ULONG -> TokenType.ULONG_RAD16
            else -> throw IllegalArgumentException()
        }
    };

    abstract fun apply(original: TokenType): TokenType
}
