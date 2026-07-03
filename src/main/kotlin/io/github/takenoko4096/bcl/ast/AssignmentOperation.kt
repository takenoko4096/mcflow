package io.github.takenoko4096.bcl.ast

enum class AssignmentOperation(val symbol: String, val origin: BinaryOperation?) {
    ASSIGN("=", null),
    PLUS_ASSIGN("+=", BinaryOperation.PLUS),
    MINUS_ASSIGN("-=", BinaryOperation.MINUS),
    MULTIPLY_ASSIGN("*=", BinaryOperation.MULTIPLY),
    DIVIDE_ASSIGN("/=", BinaryOperation.DIVIDE),
    MOD_ASSIGN("%=", BinaryOperation.MOD),
    POWER_ASSIGN("**=", BinaryOperation.POWER),
    BIT_AND_ASSIGN("&=", BinaryOperation.BIT_AND),
    BIT_OR_ASSIGN("|=", BinaryOperation.BIT_OR),
    BIT_XOR_ASSIGN("^=", BinaryOperation.BIT_XOR),
    BIT_SHIFT_LEFT_ASSIGN("<<=", BinaryOperation.BIT_SHIFT_LEFT),
    BIT_SHIFT_RIGHT_ASSIGN(">>=", BinaryOperation.BIT_SHIFT_RIGHT),
    BIT_SHIFT_RIGHT_UNSIGNED_ASSIGN(">>>=", BinaryOperation.BIT_SHIFT_RIGHT_UNSIGNED)
}
