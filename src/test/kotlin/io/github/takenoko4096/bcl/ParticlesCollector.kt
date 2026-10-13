package io.github.takenoko4096.bcl

import io.github.takenoko4096.json.JsonFile
import io.github.takenoko4096.json.JsonPath
import io.github.takenoko4096.json.JsonValueTypes
import java.io.PrintStream
import kotlin.io.path.Path

fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    println(JsonFile("src/test/resources/particles/shriek.json").toFile().readLines().joinToString("\n"))

    val names = Path("src/test/resources/particles").toFile().listFiles()!!.map {
        JsonFile(it).readAsObject().getOrThrow(
            JsonPath.of("particle_effect.description.identifier"),
            JsonValueTypes.STRING
        ).value
    }

    println(names.joinToString(",\n    ", prefix = "listOf(\n    ", postfix = "\n)") {
        "\"$it\""
    })
}