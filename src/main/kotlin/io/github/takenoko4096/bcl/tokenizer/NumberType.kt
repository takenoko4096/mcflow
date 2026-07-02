package io.github.takenoko4096.bcl.tokenizer

enum class NumberType(val char: Char) {
    INT('i') {
        override fun default(signed: Boolean): TokenType {
            return if (signed) TokenType.INT else TokenType.UINT
        }
    },
    LONG('l') {
        override fun default(signed: Boolean): TokenType {
            return if (signed) TokenType.LONG else TokenType.ULONG
        }
    },
    FLOAT('f') {
        override fun default(signed: Boolean): TokenType {
            return TokenType.FLOAT
        }
    },
    DOUBLE('d') {
        override fun default(signed: Boolean): TokenType {
            return TokenType.DOUBLE
        }
    };

    abstract fun default(signed: Boolean): TokenType
}
