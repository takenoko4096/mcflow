#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

#include "tree_sitter/parser.h"

enum TokenType {
    COMMAND_START,
    COMMAND_PART,
    TOP_LEVEL_COMMENT_COMMAND,
    INNER_COMMENT_COMMAND,
    TRIVIA
};

typedef struct {
    bool command_open;
    bool continuation_pending;
} Scanner;

static bool is_newline(int32_t c) {
    return c == '\n' || c == '\r';
}

static void advance(TSLexer *lexer) {
    lexer->advance(lexer, false);
}

static void consume_newline(TSLexer *lexer) {
    int32_t first = lexer->lookahead;
    advance(lexer);
    if (first == '\r' && lexer->lookahead == '\n') {
        advance(lexer);
    }
}

static bool scan_line_comment(TSLexer *lexer) {
    // The leading '#' has already been consumed.
    while (!lexer->eof(lexer) && !is_newline(lexer->lookahead)) {
        advance(lexer);
    }
    lexer->mark_end(lexer);
    lexer->result_symbol = TRIVIA;
    return true;
}

static bool scan_block_comment(TSLexer *lexer) {
    // The opening "#::" has already been consumed.
    uint32_t depth = 1;

    while (!lexer->eof(lexer) && depth > 0) {
        if (lexer->lookahead == '#') {
            advance(lexer);
            if (lexer->lookahead == ':') {
                advance(lexer);
                if (lexer->lookahead == ':') {
                    advance(lexer);
                    depth++;
                }
            }
            continue;
        }

        if (lexer->lookahead == ':') {
            advance(lexer);
            if (lexer->lookahead == ':') {
                advance(lexer);
                if (lexer->lookahead == '#') {
                    advance(lexer);
                    depth--;
                }
            }
            continue;
        }

        advance(lexer);
    }

    lexer->mark_end(lexer);
    lexer->result_symbol = TRIVIA;
    return true;
}

static bool is_horizontal_space(int32_t c) {
    return c == ' ' || c == '\t';
}

static bool scan_comment_or_trivia(Scanner *scanner, TSLexer *lexer, const bool *valid_symbols) {
    if (is_horizontal_space(lexer->lookahead)) {
        do {
            advance(lexer);
        }
        while (is_horizontal_space(lexer->lookahead));

        lexer->mark_end(lexer);
        lexer->result_symbol = TRIVIA;
        return true;
    }

    if (lexer->lookahead == '#') {
        // Consume '#' so that the following character can be inspected.
        advance(lexer);

        if (lexer->lookahead == '/') {
        // A comment command takes precedence over an ordinary line comment.
            if (scanner->command_open) {
                if (!valid_symbols[INNER_COMMENT_COMMAND]) return false;
                advance(lexer); // '/'
                while (!lexer->eof(lexer) && !is_newline(lexer->lookahead)) {
                    advance(lexer);
                }
                lexer->mark_end(lexer);
                lexer->result_symbol = INNER_COMMENT_COMMAND;
                return true;
            }

            if (!valid_symbols[TOP_LEVEL_COMMENT_COMMAND]) return false;
            advance(lexer); // '/'
            while (!lexer->eof(lexer) && !is_newline(lexer->lookahead)) {
                advance(lexer);
            }
            lexer->mark_end(lexer);
            lexer->result_symbol = TOP_LEVEL_COMMENT_COMMAND;
            return true;
        }

        if (lexer->lookahead == ':') {
            advance(lexer);
            if (lexer->lookahead == ':') {
                advance(lexer);
                return scan_block_comment(lexer);
            }
        }

    // Any other '#' begins a line comment. If one or two colons were
    // consumed while checking for "#::", they remain part of the comment.
        return scan_line_comment(lexer);
    }

    if (is_newline(lexer->lookahead)) {
        consume_newline(lexer);

        if (scanner->command_open && !scanner->continuation_pending) {
            scanner->command_open = false;
        }

        lexer->mark_end(lexer);
        lexer->result_symbol = TRIVIA;
        return true;
    }

    return false;
}

static bool scan_command_content(Scanner *scanner, TSLexer *lexer, const bool *valid_symbols) {
    bool is_start = !scanner->command_open;
    if (is_start) {
        if (!valid_symbols[COMMAND_START]) return false;
    }
    else {
        if (!valid_symbols[COMMAND_PART]) return false;
    }

    bool in_quote = false;
    bool consumed = false;

    while (!lexer->eof(lexer)) {
        int32_t c = lexer->lookahead;

        if (!in_quote) {
            if (c == '#' || is_newline(c) || c == ' ' || c == '\t') {
                break;
            }

            if (c == '\\') {
                // A backslash + physical newline is a continuation only inside an
                // already-open command. If command text precedes it in this token,
                // finish the token immediately before the backslash; the next scan
                // will consume the continuation as TRIVIA.
                bool had_content = consumed;
                advance(lexer);
                if (is_newline(lexer->lookahead)) {
                    if (had_content) {
                        scanner->command_open = true;
                        lexer->result_symbol = is_start ? COMMAND_START : COMMAND_PART;
                        return true; // mark_end is still before the backslash.
                    }

                    if (!is_start) {
                        if (!valid_symbols[TRIVIA]) return false;
                        consume_newline(lexer);
                        scanner->continuation_pending = true;
                        lexer->mark_end(lexer);
                        lexer->result_symbol = TRIVIA;
                        return true;
                    }

                    // A backslash at the very beginning of a command is not a
                    // continuation because there is no preceding command text.
                    consumed = true;
                    lexer->mark_end(lexer);
                    continue;
                }

                consumed = true;
                lexer->mark_end(lexer);
                continue;
            }

            if (c == '"') {
                in_quote = true;
                advance(lexer);
                consumed = true;
                lexer->mark_end(lexer);
                continue;
            }

            advance(lexer);
            consumed = true;
            lexer->mark_end(lexer);
            continue;
        }

        // Inside a quoted string, comments and line continuations are inert.
        if (is_newline(c)) {
            break;
        }

        if (c == '\\') {
            advance(lexer);
            consumed = true;
            if (!lexer->eof(lexer) && !is_newline(lexer->lookahead)) {
                advance(lexer);
            }
            lexer->mark_end(lexer);
            continue;
        }

        if (c == '"') {
            in_quote = false;
        }

        advance(lexer);
        consumed = true;
        lexer->mark_end(lexer);
    }

    if (!consumed) return false;

    scanner->command_open = true;
    if (!is_start) {
        scanner->continuation_pending = false;
    }
    else {
        scanner->continuation_pending = false;
    }

    lexer->result_symbol = is_start ? COMMAND_START : COMMAND_PART;
    return true;
}

void *tree_sitter_mcflow_external_scanner_create(void) {
    Scanner *scanner = calloc(1, sizeof(Scanner));
    return scanner;
}

void tree_sitter_mcflow_external_scanner_destroy(void *payload) {
    free(payload);
}

unsigned tree_sitter_mcflow_external_scanner_serialize(void *payload, char *buffer) {
    Scanner *scanner = (Scanner *)payload;
    buffer[0] = (scanner->command_open ? 1 : 0) | (scanner->continuation_pending ? 2 : 0);
    return 1;
}

void tree_sitter_mcflow_external_scanner_deserialize(void *payload, const char *buffer, unsigned length) {
    Scanner *scanner = (Scanner *)payload;
    scanner->command_open = false;
    scanner->continuation_pending = false;

    if (length > 0) {
        uint8_t flags = (uint8_t)buffer[0];
        scanner->command_open = (flags & 1) != 0;
        scanner->continuation_pending = (flags & 2) != 0;
    }
}

bool tree_sitter_mcflow_external_scanner_scan(void *payload, TSLexer *lexer, const bool *valid_symbols) {
    Scanner *scanner = (Scanner *)payload;

    // Work on a copy: failed scans must not mutate persistent scanner state.
    Scanner next = *scanner;

    if (valid_symbols[TRIVIA] && (lexer->lookahead == '#' || is_newline(lexer->lookahead) || is_horizontal_space(lexer->lookahead))) {
        if (scan_comment_or_trivia(&next, lexer, valid_symbols)) {
            *scanner = next;
            return true;
        }
        // The scanner consumed input while checking a comment prefix. If the
        // corresponding token is not valid in this parser state, do not continue
        // scanning from the advanced position.
        return false;
    }

    // Ordinary command text is considered only after comment/newline handling.
    if (valid_symbols[COMMAND_START] || valid_symbols[COMMAND_PART]) {
        next = *scanner;
        if (scan_command_content(&next, lexer, valid_symbols)) {
            *scanner = next;
            return true;
        }
    }

    return false;
}
