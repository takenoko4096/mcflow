package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.brigadier.CommandDispatcher
import io.github.takenoko4096.bcl.brigadier.argument.BooleanArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.IntegerArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.StringArgumentType
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.bcl.tokenizer.Token
import io.github.takenoko4096.json.JsonFile
import java.io.PrintStream

fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    val dispatcher = CommandDispatcher()
    dispatcher.register("return") {
        description("return an integer value.")

        aliases("ret")

        "int_val"(IntegerArgumentType.unlimited()) {
            executable()

            "flg"(BooleanArgumentType()) {
                executable()
            }
        }

        "fail" {
            executable()
        }
    }

    dispatcher.register("say") {
        description("saying!")

        aliases("shaberu")

        "greedy_str"(StringArgumentType.greedy()) {
            executable()

            "count"(IntegerArgumentType.unlimited()) {
                executable()
            }
        }
    }

    println(dispatcher.getCommand("say")!!.getAllUsages())

    val text = """
        //ret fail
    """.trimIndent()

    val r = JsonReflector(Node::class.java)

    val f = JsonFile("src/test/resources/out.json")
    if (!f.exists()) f.create()

    val parseResult = BclParser(
        text,
        BclTokenizer(text).tokenize(),
        dispatcher,
        true
    ).parse()

    for (node in parseResult.nodes.filterIsInstance<CommandLineParseResult>()) {
        when (node) {
            is CommandLineParseResult.Failure -> println(node.diagnostics)
            is CommandLineParseResult.Successful -> println(node)
        }
    }

    val o = r.reflect(parseResult)

    f.write(o)

    println(BclTokenizer("execute as@a[tag=a]at@s facing^1^^run tp~1~~2~~true").tokenize().map(Token::value))
}
