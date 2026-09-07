package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.brigadier.CommandDispatcher
import io.github.takenoko4096.bcl.brigadier.argument.BlockPropertiesArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.BooleanArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.EnumArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.BlockTypeArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.FloatArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.IntegerArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.PositionArgumentType
import io.github.takenoko4096.bcl.brigadier.argument.StringArgumentType
import io.github.takenoko4096.bcl.brigadier.value.Difficluty
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.json.JsonFile
import java.io.PrintStream

fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    val dispatcher = CommandDispatcher()
    dispatcher.register("return") {
        description = "return an integer value."

        aliases("ret")

        "int_val"(FloatArgumentType.float()) {
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
        description = "say a message"

        aliases("shaberu")

        "greedy_str"(StringArgumentType.greedy()) {
            executable()

            "count"(IntegerArgumentType.unlimited()) {
                executable()
            }
        }
    }

    dispatcher.register("setblock") {
        "position"(PositionArgumentType()) {
            "name"(BlockTypeArgumentType()) {
                executable()

                "properties"(BlockPropertiesArgumentType()) {
                    executable()
                }
            }
        }
    }

    dispatcher.register("difficulty") {
        "mode"(EnumArgumentType.of<Difficluty>()) {
            executable()
        }
    }

    println(dispatcher.getCommand("say")!!.getAllUsages())

    val text = """
        /ret -5.55
        setblock ~ ~ ~ minecraft:air["age"=2, minecraft:wwww=4]
        difficulty peaceful
        say i am a pen!!!!!!!!
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
        println(node)
    }

    val o = r.reflect(parseResult)

    f.write(o)

    val s = "execute as@a[tag=a]at@s facing^1^^run tp~-1~~-2~~true"
    println(
        BclTokenizer(s)
            .tokenize()
            .map { it.sourceSpan.getStringFromSourceCode(s) }
    )
}
