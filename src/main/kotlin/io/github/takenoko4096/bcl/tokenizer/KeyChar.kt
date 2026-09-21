package io.github.takenoko4096.bcl.tokenizer

object KeyChar {
    const val SPACE = ' '
    const val TAB = '\t'
    const val CARRIAGE_RETURN = '\r'
    const val LINEBREAK = '\n'

    const val DECIMAL_POINT = '.'
    const val ZERO = '0'
    const val EXPONENTIAL = 'e'

    const val UNSIGNED = 'u'

    const val CHAR_QUOTE = '\''
    const val STRING_QUOTE = '"'
    const val SPECIAL_STRING_QUOTE = '`'
    const val ESCAPE = '\\'
    const val SLASH = '/'

    val WHITESPACE_CHARS = setOf(SPACE, TAB, LINEBREAK)
    val QUOTE_CHARS = setOf(STRING_QUOTE, SPECIAL_STRING_QUOTE)
    val NUMBER_CHAR_SEQUENCE = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    const val LINE_COMMENT_SEQUENCE = "#"
    const val BLOCK_COMMENT_START_SEQUENCE = "/*"
    const val BLOCK_COMMENT_END_SEQUENCE = "*/"

    const val WINDOWS_LINE_SEPARATOR_SEQUENCE = "$CARRIAGE_RETURN$LINEBREAK"
    const val MAC_LINE_SEPARATOR = CARRIAGE_RETURN

    val CONTROL_CHARS = mapOf(
        'r' to '\r',
        'n' to '\n',
        't' to '\t',
        '\\' to '\\'
    )

    const val COORDINATE_RELATIVE = '~'
    const val COORDINATE_LOCAL = '^'
    const val PLUS = '+'
    const val MINUS = '-'
    const val WILDCARD = '*'

    val COMMAND_WORD_TERMINATORS = charArrayOf(
        ' ', '{', '}', '[', ']', ',', '!', ';', '"', '\'', '`', '@', '=', '~', '^', '|', '/', '+', '#', '$', '<','>', '*', '&', '\r', '\n', '\t'
    )

    const val COLON = ':'

    const val DOT = '.'

    val SYMBOLS = charArrayOf(
        ' ', '{', '}', '[', ']', ',', '!', ';', '"', '\'', '`', '@', '=','~', '^', '|', '/', '#', '$',
        '<','>', '*', '+', '&', '\r', '\n', '\t', '.', ':', '-', '\\'
    )
}
