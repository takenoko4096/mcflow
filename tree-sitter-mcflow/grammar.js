// @ts-check
/**
 * @file Mcflow grammar for tree-sitter
 * @author takenoko4096
 * @license MIT
 */

/// <reference types="tree-sitter-cli/dsl" />

/**
 * '\r', '\t' が無いことは呼び出し側によって保証される
 */
export default grammar({
    name: "mcflow",
    rules: {
        source_file: $ => repeat($.command_line),

        command_line: $ => seq(
            optional('/'),
            repeat1(choice(
                $.command_text,
                $.line_continuation
            ))
        ),

        command_text: () => token(/[^\\\n]+/),

        line_continuation: $ => seq(
            '\\',
            $.newline,
            /\s*/
        ),

        newline: () => /\n/
    }
});
