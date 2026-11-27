package io.github.takenoko4096.bcl

import io.github.treesitter.jtreesitter.Language
import io.github.treesitter.jtreesitter.Parser
import io.github.treesitter.jtreesitter.mcflow.TreeSitterMcflow
import java.io.PrintStream

fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    val language = Language(TreeSitterMcflow.language())
    val parser = Parser(language)

    val tree = parser.parse(
        """
        # 01. 通常のコマンドと空白
        say hello
            say indented
        say    multiple   spaces
        say	multiple	tabs

        # 02. 行コメント
        say before # ordinary line comment
        say after

        # 行コメントの内部にある特殊構文は認識されない
        # ordinary comment containing #:: block syntax
        # ordinary comment containing #/ command syntax
        # ordinary comment containing \ continuation syntax

        say after line comments

        # 03. ブロックコメント
        #:: standalone block comment
            this is not a command
            #/ this is not a comment command
            # ordinary-looking line comment
            \ this is not a continuation
        ::#

        say after standalone block comment

        # ネストしたブロックコメント
        say before #:: outer block
            #/ not a comment command
            # ordinary-looking line comment
            #:: nested block
                nested contents
            ::#
            still inside outer block
        ::# say after nested block

        # 04. コマンド中のブロックコメント
        say before#:: comment ::#after
        say before #:: multiline
            block comment
        ::# after

        # 05. 文字列内の特殊構文
        say "inside #/ not a comment command"
        say "inside #:: not a block comment ::#"
        say "inside \ not a continuation"
        say "inside # and ::#"

        # 06. コメントコマンド
        #/ top-level comment command
            #/ indented top-level comment command
        #/ special characters: #:: ::# \ " spaces

        say after top-level comment commands

        # 07. 行継続
        say first \
            second

        say first \

            second

        say first \
            # ordinary line comment during continuation
            second

        say first \
            #:: block comment during continuation
                block contents
            ::#
            second

        # 08. 行継続中のコメントコマンド
        say first \
            #/ inner comment command
            second

        say first \
            #/ inner comment with #:: and ::# and \
            second

        # 09. 複数回の行継続
        say first \
            second \
            third

        say first \
            # ordinary comment
            second \
            #:: block comment ::#
            third

        say first \
            #/ first inner comment
            second \
            #/ second inner comment
            third

        # 10. 通常の改行によるコマンドの分離
        say command one
        say command two

        # 11. コメントとコマンドの境界
        say before # trailing comment
        #/ standalone comment command
        say after

        # 12. ファイル末尾のコマンド
        say final command
        """.trimIndent()
    ).orElseThrow()

    println(tree.rootNode)
}
