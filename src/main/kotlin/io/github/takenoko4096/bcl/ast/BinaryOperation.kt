package io.github.takenoko4096.bcl.ast

enum class BinaryOperation(val symbol: String) {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MOD("%"),
    POWER("**"),
    BIT_AND("&"),
    BIT_OR("|"),
    BIT_XOR("^"),
    BIT_SHIFT_LEFT("<<"),
    BIT_SHIFT_RIGHT(">>"),
    BIT_SHIFT_RIGHT_UNSIGNED(">>>"),
    LOGICAL_AND("&&"),
    LOGICAL_OR("||"),
    LOGICAL_MORE(">"),
    LOGICAL_LESS("<"),
    LOGICAL_MORE_OR_EQUALS(">="),
    LOGICAL_LESS_OR_EQUALS("<="),
    LOGICAL_EQUALS("=="),
    LOGICAL_NOT_EQUALS("!=")
}
