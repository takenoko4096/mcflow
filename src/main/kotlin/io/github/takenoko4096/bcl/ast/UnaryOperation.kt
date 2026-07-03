package io.github.takenoko4096.bcl.ast

enum class UnaryOperation(val symbol: String) {
    LOGICAL_NOT("!"),
    BIT_INVERSE("~"),
    PLUS("+"),
    MINUS("-"),
    INCREMENT("++"),
    DECREMENT("--")
}
