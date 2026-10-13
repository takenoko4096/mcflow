package io.github.takenoko4096.bcl

import io.github.takenoko4096.json.JsonFile
import java.io.PrintStream
import kotlin.io.path.Path

fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    val names = Path("src/test/resources/particles").toFile().listFiles()!!.map {
        "minecraft:" + it.nameWithoutExtension
    }

    println(names.joinToString(",\n    ", prefix = "listOf(\n    ", postfix = "\n)") {
        "\"$it\""
    })
}