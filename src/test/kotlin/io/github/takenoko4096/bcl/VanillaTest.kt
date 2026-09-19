package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.brigadier.CommandDispatcher
import io.github.takenoko4096.bcl.brigadier.CommandLineParseResult
import java.io.PrintStream

fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    val dispatcher = CommandDispatcher("root")

    Commands.getAllVanillaCommandBuilders().forEach(dispatcher::register)

    val result = dispatcher.evaluate("""
    
    execute if score @s foo = @s bar run list
    """)

    // TODO execute if score だけ　なぜかリダイレクトできない
    // redirects が空？？？？？？？？？？？？？？？？？？

    println(dispatcher.getCommand("execute")!!.children["if"]!!.children["score"]!!.children["target1"]!!.children["objective1"]!!.children["matches"]!!.redirects)

    result.forEach(::println)
    result.filterIsInstance<CommandLineParseResult.Successful>().map { it.resultValue }.forEach(::println)
}
