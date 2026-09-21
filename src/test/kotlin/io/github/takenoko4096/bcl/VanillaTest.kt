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
    execute if score @s foo = @s bar run tell @a f
    music volume 3
    ? list
    packstack client verbose \
        exclude-vanilla
    fill 7 2 12 7 4 18 composter ["composter_fill_level"=8] replace structure_void
    place jigsaw +/a----a/    ------         /-a foo 1
    execute as @p at @s positioned ~0.5~~0.5 rotated as @e[tag=a] positioned ^^^0.7071 rotated as @e[tag=a] positioned ^^^1.4142 if entity @s[r=1.5,rm=0.1] run list
    recipe take @s *
    schedule delay add fn/4 0
    """.trimIndent())

    result.forEach(::println)
    result.filterIsInstance<CommandLineParseResult.Successful>().map { it.resultValue }.forEach(::println)
}
