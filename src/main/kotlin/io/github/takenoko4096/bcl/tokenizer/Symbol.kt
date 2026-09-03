package io.github.takenoko4096.bcl.tokenizer

enum class Symbol(val value: String, val expressionStartable: Boolean) {
    COMMA(',', false),
    DOT('.', false),
    SEMICOLON (';', false),
    PARENTHESIS_START('(', true),
    PARENTHESIS_END(')', false),
    BRACE_START('{', true),
    BRACE_END('}', false),
    ARRAY_START('[', true),
    ARRAY_END(']', false),
    ASSIGN('=', false),
    PLUS_ASSIGN("+=", false),
    MINUS_ASSIGN("-=", false),
    MULTIPLY_ASSIGN("*=", false),
    DIVIDE_ASSIGN("/=", false),
    MOD_ASSIGN("%=", false),
    BIT_AND_ASSIGN("&=", false),
    BIT_OR_ASSIGN("|=", false),
    BIT_XOR_ASSIGN("^=", false),
    POWER_ASSIGN("**=", false),
    LOGICAL_EQUALS("==", false),
    LOGICAL_LESS('<', false),
    LOGICAL_MORE('>', false),
    LOGICAL_LESS_OR_EQUALS("<=", false),
    LOGICAL_MORE_OR_EQUALS(">=", false),
    LOGICAL_NOT_EQUALS("!=", false),
    COLON(':', false),
    PLUS('+', true),
    MINUS('-', true),
    MULTIPLY('*', false),
    DIVIDE('/', false),
    MOD('%', false),
    POWER("**", false),
    LOGICAL_AND("&&", false),
    LOGICAL_OR("||", false),
    LOGICAL_NOT('!', true),
    BIT_NOT("~!", true),
    BIT_AND('&', false),
    BIT_OR('|', false),
    BIT_XOR('^', false),
    BIT_SHIFT_LEFT_ASSIGN("<<=", false),
    BIT_SHIFT_RIGHT_ASSIGN(">>=", false),
    BIT_SHIFT_RIGHT_UNSIGNED_ASSIGN(">>>=", false),
    OPTIONAL_CHAIN("?.", false),
    ELVIS("?:", false),
    ARROW("->", false),
    INCREMENT("++", true),
    DECREMENT("--", true),
    QUESTION('?', false),
    NAMESPACE_RESOLUTION("::", false),
    AT('@', false),
    NEWLINE('\n', false),
    HASH('#', false),
    RANGE("..", false),
    ESCAPE('\\', false);

    constructor(char: Char, expressionStartable: Boolean) : this(char.toString(), expressionStartable)

    companion object {
        fun getExpressionStartableSymbols(): Set<Symbol> {
            return entries.filter(Symbol::expressionStartable).toSet()
        }
    }
}
