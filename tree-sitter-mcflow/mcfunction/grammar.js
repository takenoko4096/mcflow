// @ts-check

/// <reference types="tree-sitter-cli/dsl" />

export default grammar({
    name: "mcfunction",

    extras: $ => [],

    rules: {
        source_file: $ => seq(
            repeat(choice(
                seq($.command_line, optional($._newline)),
                seq(optional($._indent), $.comment_command, optional($._newline)),
                seq(optional($._indent), $.line_comment, optional($._newline)),
                $._blank_line,
            )),
            optional($._indent),
        ),

        command_line: $ => prec.right(seq(
            optional($._indent),
            repeat1(choice(
                $.string,
                $.command_content,
            )),
            optional(choice(
                $.line_comment,
                $.comment_command,
            )),
        )),

        comment_command: $ => token(prec(2, seq(
            "#/",
            /[^\r\n]*/,
        ))),

        line_comment: $ => token(prec(1, seq(
            "#",
            /[^\r\n]*/,
        ))),

        string: $ => token(seq(
            '"',
            repeat(choice(
                /[^"\\\r\n]+/,
                /\\[^\r\n]/,
            )),
            '"',
        )),

        command_content: $ => token(
            /[^#"\r\n]+/
        ),

        _indent: $ => token(prec(1, /[ \t]+/)),

        _newline: $ => token(/\r\n|\r|\n/),

        _blank_line: $ => seq(
            optional($._indent),
            $._newline,
        ),
    },
});

