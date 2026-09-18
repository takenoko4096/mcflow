package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.ast.Node
import io.github.takenoko4096.bcl.brigadier.CommandDispatcher
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import io.github.takenoko4096.bcl.brigadier.argument.*
import io.github.takenoko4096.bcl.brigadier.value.block.BlockProperties
import io.github.takenoko4096.bcl.brigadier.value.block.BlockType
import io.github.takenoko4096.bcl.brigadier.value.entity.EntitySelector
import io.github.takenoko4096.bcl.brigadier.value.location.Coordinate
import io.github.takenoko4096.bcl.parser.BclParser
import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import io.github.takenoko4096.json.JsonFile
import java.io.PrintStream

fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    val dispatcher = CommandDispatcher("root")
    dispatcher.register("return") {
        description = "return an integer value."

        aliases.add("ret")

        "int_val"(FloatArgumentType.float()) {
            executes { getArgument<Float>("int_val").value }

            "flg"(BooleanArgumentType()) {
                executes {
                    getArgument<Boolean>("flg").value
                }
            }
        }

        "fail" {
            executes {
                "failure!!!"
            }
        }
    }

    dispatcher.register("say") {
        description = "say a message"

        aliases.add("shaberu")

        "greedy_str"(StringArgumentType.greedy()) {
            executes {
                getArgument<String>("greedy_str").value
            }

            "count"(IntegerArgumentType.radixUnlimited()) {
                executes {
                    val s = getArgument<String>("greedy_str").value
                    s.repeat(getArgument<Int>("count").value)
                }
            }
        }
    }

    dispatcher.register("setblock") {
        "position"(PositionArgumentType()) {
            "name"(BlockTypeArgumentType()) {
                executes {
                    "${getValue<Coordinate>("position")} に ${getValue<BlockType>("name")} を設置"
                }

                "properties"(BlockPropertiesArgumentType()) {
                    executes {
                        val blockType = getValue<BlockType>("name")
                        val properties = getArgument<BlockProperties>("properties")

                        for ((property, _) in properties.value) {
                            if (property !in blockType) {
                                throw exception(properties.sourceSpan, "プロパティ '${property.name}' はブロック '${blockType.identifier}' に存在しません")
                            }
                        }
                    }
                }
            }
        }
    }

    dispatcher.register("sel") {
        "selc"(EntitySelectorArgumentType()) {
            executes {
                "${getValue<EntitySelector>("selc")}"
            }
        }
    }

    Commands.getAllVanillaCommandBuilders().forEach(dispatcher::register)

    println(dispatcher.getCommand("say")!!.getAllUsages())

    val text = """
        /ret -5.55
        setblock ~ ~ ~ minecraft:potatoes["growth"=1]
        difficulty 2
        say i am a pen!!!!!!!!
        sel @e[x=~1, y=-0.4, z = ~,c=1, type=!evocation_fang,\
        name= "ff\"ff ", family=foo.bar-baz?, name=!a:b::::::c:::,\
         hasitem={item=minecraft:apple,location=slot.weapon.mainhand}, \
         scores={a=!1..3, b=..2, c=1.., d=4, e=-4..-2}, has_property={property=!minecraft:big}\
         ]
        @set main {}
    """.trimIndent()

    val r = JsonReflector(Node::class.java)

    val f = JsonFile("src/test/resources/out.json")
    if (!f.exists()) f.create()

    val parseResult = BclParser(
        text,
        BclTokenizer(text, interpretComments = false).tokenize(),
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
        BclTokenizer(s, interpretComments = false)
            .tokenize()
            .map { it.sourceSpan.getStringFromSourceCode(s) }
    )

    val results = dispatcher.evaluate("fill ~~~^^^20 composter[\"composter_fill_level\"=8] replace air")

    for (result in results) {
        when (result) {
            is CommandLineParseResult.Failure -> {
                println(result)
            }
            is CommandLineParseResult.Successful -> {
                println(result)
            }
        }
    }
}
