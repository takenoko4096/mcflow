package io.github.takenoko4096.bcl

import io.github.takenoko4096.bcl.tokenizer.BclTokenizer
import java.io.PrintStream

fun main() {
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    println(
        BclTokenizer(
            """
            execute as @e[type=player, name=Steve, hasitem={item=apple, data=1, amount = 2..5}] at @s run {
                say a
                say b
                kill
            }
        """.trimIndent()
        ).tokenize()
    )
}
