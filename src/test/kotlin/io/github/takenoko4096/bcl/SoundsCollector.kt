package io.github.takenoko4096.bcl

import io.github.takenoko4096.json.JsonFile
import io.github.takenoko4096.json.JsonValueTypes
import java.io.PrintStream
import kotlin.io.path.Path

fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    val file = JsonFile("src/test/resources/sound_definitions.json")
    val keys = file.readAsObject().getOrThrow("sound_definitions", JsonValueTypes.OBJECT).keys()

    println(keys.joinToString(",\n", prefix = "listOf(\n", postfix = "\n)") {
        "    \"$it\""
    })
}
