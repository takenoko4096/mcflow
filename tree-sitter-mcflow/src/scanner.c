#include "tree_sitter/parser.h"

#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

enum TokenType {
    COMMAND_START,
    COMMAND_CONTENT,

    CONTINUED_COMMENT_COMMAND,
    STANDALONE_COMMENT_COMMAND,

    TRIVIA,

    ERROR_SENTINEL,
};

typedef struct {
    bool in_command;
    bool continuation_pending;
    bool in_quote;
} Scanner;

static void advance(TSLexer *lexer, bool skip) {
    lexer->advance(lexer, skip);
}

static bool is_eof(TSLexer *lexer) {
    return lexer->eof(lexer);
}

static bool is_newline(TSLexer *lexer) {
    return lexer->lookahead == '\n';
}

static bool is_space(int32_t c) {
    return c == ' ' || c == '\t';
}

static bool emit_token(
    TSLexer *lexer,
    TSSymbol symbol
) {
    lexer->mark_end(lexer);
    lexer->result_symbol = symbol;
    return true;
}

/*
 * Consume a nested block comment.
 *
 * The opening '#::' has already been consumed.
 */
static void consume_block_comment(TSLexer *lexer) {
    unsigned depth = 1;

    while (depth > 0 && !is_eof(lexer)) {
        if (lexer->lookahead == '#') {
            advance(lexer, false);

            if (lexer->lookahead == ':') {
                advance(lexer, false);

                if (lexer->lookahead == ':') {
                    advance(lexer, false);
                    depth++;
                }
            }
        }
        else if (lexer->lookahead == ':') {
            advance(lexer, false);

            if (lexer->lookahead == ':') {
                advance(lexer, false);

                if (lexer->lookahead == '#') {
                    advance(lexer, false);
                    depth--;
                }
            }
        }
        else {
            advance(lexer, false);
        }
    }
}

/*
 * Consume a line comment.
 *
 * The '#' has already been consumed.
 * The newline is deliberately left to the caller.
 */
static void consume_line_comment(TSLexer *lexer) {
    while (!is_eof(lexer) && !is_newline(lexer)) {
        advance(lexer, false);
    }
}

/*
 * Scan trivia and comment commands.
 *
 * This function is also responsible for ending a command:
 * a normal newline or a trailing line comment resets
 * in_command.
 *
 * It never returns false after consuming input.
 */
static bool scan_trivia(
    Scanner *scanner,
    TSLexer *lexer,
    const bool *valid_symbols
) {
    bool consumed = false;

    while (!is_eof(lexer)) {
        /*
         * Inside a quoted string, only a physical newline
         * is relevant to trivia scanning.
         *
         * Other characters, including '#', belong to the
         * quoted string and are handled by COMMAND_CONTENT.
         */
        if (scanner->in_command && scanner->in_quote) {
            if (!is_newline(lexer)) {
                break;
            }

            advance(lexer, false);

            scanner->in_command = false;
            scanner->continuation_pending = false;
            scanner->in_quote = false;

            consumed = true;
            lexer->mark_end(lexer);
            continue;
        }

        /*
         * A normal command newline terminates the command.
         * A newline while continuation_pending is true
         * is merely a blank line in the continuation area.
         */
        if (is_newline(lexer)) {
            advance(lexer, false);

            if (scanner->in_command &&
                !scanner->continuation_pending) {
                scanner->in_command = false;
                scanner->in_quote = false;
            }

            consumed = true;
            lexer->mark_end(lexer);
            continue;
        }

        /*
         * Horizontal whitespace:
         *
         * - At the top level: trivia.
         * - During continuation: trivia.
         * - In ordinary command content: actual content.
         */
        if (is_space(lexer->lookahead)) {
            if (scanner->in_command &&
                !scanner->continuation_pending) {
                break;
            }

            advance(lexer, false);

            consumed = true;
            lexer->mark_end(lexer);
            continue;
        }

        /*
         * A further line continuation inside the continuation
         * area is handled by COMMAND_CONTENT.
         *
         * Do not consume '\' here, because it may be ordinary
         * command content rather than a continuation marker.
         */
        if (scanner->in_command &&
            scanner->continuation_pending &&
            lexer->lookahead == '\\') {
            break;
        }

        /*
         * Anything other than '#' is the beginning of
         * actual command content.
         */
        if (lexer->lookahead != '#') {
            if (scanner->in_command &&
                scanner->continuation_pending &&
                consumed) {
                scanner->continuation_pending = false;
            }

            break;
        }

        /*
         * Inspect a comment opener.
         *
         * Save the boundary before '#'. If this is a comment
         * command and we already consumed trivia, return the
         * trivia first. The next scan will start at '#'.
         */
        lexer->mark_end(lexer);
        advance(lexer, false);

        if (lexer->lookahead == '/') {
            bool is_continued =
                scanner->in_command &&
                scanner->continuation_pending;

            bool is_standalone = !scanner->in_command;

            bool can_emit =
                (is_continued &&
                 valid_symbols[CONTINUED_COMMENT_COMMAND]) ||
                (is_standalone &&
                 valid_symbols[STANDALONE_COMMENT_COMMAND]);

            if (can_emit) {
                if (consumed) {
                    lexer->result_symbol = TRIVIA;
                    return true;
                }

                advance(lexer, false); // '/'

                consume_line_comment(lexer);

                lexer->mark_end(lexer);

                lexer->result_symbol = is_continued
                    ? CONTINUED_COMMENT_COMMAND
                    : STANDALONE_COMMENT_COMMAND;

                return true;
            }

            // Otherwise, '#/' is an ordinary line comment.
            advance(lexer, false); // '/'
            consume_line_comment(lexer);
        }
        else if (lexer->lookahead == ':') {
            advance(lexer, false);

            if (lexer->lookahead == ':') {
                advance(lexer, false);

                consume_block_comment(lexer);

                consumed = true;
                lexer->mark_end(lexer);
                continue;
            }

            // '#:' is not a block-comment opener.
            consume_line_comment(lexer);
        }
        else {
            // Ordinary line comment.
            consume_line_comment(lexer);
        }

        /*
         * A normal line comment ends the current command.
         * A line comment inside a continuation area does not.
         */
        if (scanner->in_command &&
            !scanner->continuation_pending) {
            scanner->in_command = false;
            scanner->in_quote = false;
        }

        // At the top level, and during continuation,
        // the comment line is simply trivia.
        if (is_newline(lexer)) {
            advance(lexer, false);
        }

        consumed = true;
        lexer->mark_end(lexer);
    }

    /*
     * If we reached actual content after consuming indentation
     * or comments, emit trivia first.
     */
    if (consumed) {
        lexer->result_symbol = TRIVIA;
        return true;
    }

    /*
     * No input was consumed, so returning false is safe.
     */
    return false;
}

/*
 * Scan the first character of a command.
 *
 * This token establishes the command state.
 */
static bool scan_command_start(
    Scanner *scanner,
    TSLexer *lexer
) {
    if (is_eof(lexer) ||
        is_newline(lexer) ||
        is_space(lexer->lookahead) ||
        lexer->lookahead == '#') {
        return false;
    }

    int32_t first = lexer->lookahead;

    advance(lexer, false);

    scanner->in_command = true;
    scanner->continuation_pending = false;
    scanner->in_quote = first == '"';

    /*
     * Handle the unusual case where the first character
     * itself is a line continuation marker.
     */
    if (first == '\\' && is_newline(lexer)) {
        advance(lexer, false);
        scanner->continuation_pending = true;
    }

    return emit_token(lexer, COMMAND_START);
}

/*
 * Scan one contiguous portion of command content.
 *
 * The token ends at:
 * - a physical newline,
 * - a comment opener,
 * - a line continuation marker.
 *
 * A line continuation marker is included in this token.
 */
static bool scan_command_content(
    Scanner *scanner,
    TSLexer *lexer
) {
    if (!scanner->in_command) {
        return false;
    }

    /*
     * The first actual content character after a continuation
     * ends the pending continuation area.
     */
    scanner->continuation_pending = false;

    bool consumed = false;

    while (!is_eof(lexer)) {
        int32_t c = lexer->lookahead;

        if (is_newline(lexer)) {
            break;
        }

        /*
         * Quoted string.
         *
         * Comments and line continuations are not recognized
         * inside a quoted string.
         */
        if (scanner->in_quote) {
            if (c == '\\') {
                advance(lexer, false);
                consumed = true;

                /*
                 * A backslash immediately before a newline
                 * does not continue the quoted string.
                 */
                if (is_eof(lexer) || is_newline(lexer)) {
                    lexer->mark_end(lexer);
                    break;
                }

                advance(lexer, false);
                lexer->mark_end(lexer);
                continue;
            }

            if (c == '"') {
                advance(lexer, false);
                scanner->in_quote = false;

                consumed = true;
                lexer->mark_end(lexer);
                continue;
            }

            advance(lexer, false);

            consumed = true;
            lexer->mark_end(lexer);
            continue;
        }

        /*
         * A comment opener is handled by scan_trivia.
         * Do not consume it here.
         */
        if (c == '#') {
            break;
        }

        /*
         * Start of a quoted string.
         */
        if (c == '"') {
            advance(lexer, false);

            scanner->in_quote = true;

            consumed = true;
            lexer->mark_end(lexer);
            continue;
        }

        /*
         * Line continuation outside quoted strings.
         */
        if (c == '\\') {
            advance(lexer, false);
            consumed = true;

            if (is_newline(lexer)) {
                advance(lexer, false);

                scanner->continuation_pending = true;

                lexer->mark_end(lexer);
                lexer->result_symbol = COMMAND_CONTENT;
                return true;
            }

            lexer->mark_end(lexer);
            continue;
        }

        /*
         * Ordinary command character.
         */
        advance(lexer, false);

        consumed = true;
        lexer->mark_end(lexer);
    }

    if (!consumed) {
        return false;
    }

    lexer->result_symbol = COMMAND_CONTENT;
    return true;
}

/*
 * Scanner API
 */

void *tree_sitter_mcflow_external_scanner_create(void) {
    return calloc(1, sizeof(Scanner));
}

void tree_sitter_mcflow_external_scanner_destroy(void *payload) {
    free(payload);
}

unsigned tree_sitter_mcflow_external_scanner_serialize(
    void *payload,
    char *buffer
) {
    const Scanner *scanner = (const Scanner *)payload;

    buffer[0] =
        (scanner->in_command ? 1 : 0) |
        (scanner->continuation_pending ? 2 : 0) |
        (scanner->in_quote ? 4 : 0);

    return 1;
}

void tree_sitter_mcflow_external_scanner_deserialize(
    void *payload,
    const char *buffer,
    unsigned length
) {
    Scanner *scanner = (Scanner *)payload;

    memset(scanner, 0, sizeof(Scanner));

    if (length == 0) {
        return;
    }

    scanner->in_command = (buffer[0] & 1) != 0;
    scanner->continuation_pending = (buffer[0] & 2) != 0;
    scanner->in_quote = (buffer[0] & 4) != 0;
}

bool tree_sitter_mcflow_external_scanner_scan(
    void *payload,
    TSLexer *lexer,
    const bool *valid_symbols
) {
    Scanner *scanner = (Scanner *)payload;

    /*
     * Tree-sitter may mark all symbols valid during error
     * recovery. Do not mutate scanner state in that situation.
     */
    if (valid_symbols[ERROR_SENTINEL]) {
        return false;
    }

    /*
     * Work on a copy. State is committed only when a token
     * is successfully emitted.
     */
    Scanner next = *scanner;

    /*
     * Extras have priority because they can terminate a
     * command or recognize a comment command.
     */
    if (valid_symbols[TRIVIA]) {
        if (scan_trivia(&next, lexer, valid_symbols)) {
            *scanner = next;
            return true;
        }
    }

    /*
     * The trivia scanner did not consume anything.
     * Try the token expected by the current command state.
     */
    next = *scanner;

    if (next.in_command) {
        if (valid_symbols[COMMAND_CONTENT] &&
            scan_command_content(&next, lexer)) {
            *scanner = next;
            return true;
        }

        return false;
    }

    if (valid_symbols[COMMAND_START] &&
        scan_command_start(&next, lexer)) {
        *scanner = next;
        return true;
    }

    return false;
}
