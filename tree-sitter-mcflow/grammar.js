// @ts-check

/// <reference types="tree-sitter-cli/dsl" />

export default grammar({
    name: "mcflow",

    externals: $ => [
        $._command_start,
        $._command_part,
        $._top_level_comment_command,
        $._inner_comment_command,
        $._trivia,
    ],

    // Horizontal whitespace is handled by the ordinary lexer. Newlines,
    // comments, and line continuations are handled by the external scanner.
    extras: $ => [
        $._trivia,
    ],

    rules: {
        source_file: $ => repeat(choice(
            $.command_line,
            alias($._top_level_comment_command, $.comment_command),
        )),

        command_line: $ => seq(
            $._command_start,
            repeat(choice(
                $._command_part,
                alias($._inner_comment_command, $.comment_command),
            )),
        ),
    }
});
