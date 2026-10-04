package io.github.takenoko4096.bcl.tokenizer

enum class TokenType {
    INT_RAD2,
    INT_RAD8,
    INT,
    INT_RAD16,

    UINT_RAD2,
    UINT_RAD8,
    UINT,
    UINT_RAD16,

    FLOAT,
    FLOAT_WITH_EXPONENT,

    SINGLE_QUOTED,
    DOUBLE_QUOTED,
    BACK_QUOTED,
    WORD,

    SYMBOL,

    COORDINATE_COMPONENT_RELATIVE,
    COORDINATE_COMPONENT_LOCAL,

    TIME_D,
    TIME_S,
    TIME_T,
    LEVEL,

    INVALID;

    override fun toString(): String {
        return name.lowercase()
    }
}
