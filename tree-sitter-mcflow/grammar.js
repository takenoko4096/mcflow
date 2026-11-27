// @ts-check

/// <reference types="tree-sitter-cli/dsl" />


/// <reference types="tree-sitter-cli/dsl" />

export default grammar({
    name: 'mcflow',

    externals: $ => [
        $._command_start,
        $._command_content,

        $._continued_comment_command,
        $._standalone_comment_command,

        $._trivia,

        // Error recovery 時のスキャナー誤動作を防ぐための番兵。
        $._error_sentinel,
    ],

    extras: $ => [
        $._trivia,
    ],

    rules: {
        source_file: $ => repeat(choice(
            $.command_line,
            alias($._standalone_comment_command, $.comment_command),
        )),

        command_line: $ => seq(
            $._command_start,

            repeat(choice(
                $._command_content,
                alias($._continued_comment_command, $.comment_command),
            )),
        ),
    },
});
