package io.github.takenoko4096.bcl.parser

enum class Keyword(val value: String) {
    VAR("var"),
    CONST("const"),
    MACRO("macro"),
    RETURN("return"),
    IF("if"),
    ELSE("else"),
    FOR("for"),
    IN("in"),
    WHILE("while"),
    DO("do"),
    CONTINUE("continue"),
    BREAK("break")
}
