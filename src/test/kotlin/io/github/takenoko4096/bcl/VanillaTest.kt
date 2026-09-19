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
    fill ~~-1 1^-4^ ^20 \
        composter[  "composter_fill_level" =8] \
            replace air

    /help execute

    /execute as@a at@s if block~~~air align x run gamemode s
    
    execute if entity @e[name=a]
    """)

    result.forEach(::println)
    result.filterIsInstance<CommandLineParseResult.Successful>().map { it.resultValue }.forEach(::println)
}
