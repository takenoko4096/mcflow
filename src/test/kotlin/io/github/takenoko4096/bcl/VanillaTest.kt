package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.brigadier.CommandDispatcher
import java.io.PrintStream

fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    val dispatcher = CommandDispatcher()
    Commands.getAllVanillaCommands().forEach(dispatcher::register)

    val result = dispatcher.evaluate("""
    fill ~~-1 1^-4^ ^20 \
        composter[  "composter_fill_level" =99] \
            replace air
    """)

    result.forEach(::println)
}
