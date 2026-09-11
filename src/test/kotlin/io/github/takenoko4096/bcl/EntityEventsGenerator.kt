package io.github.takenoko4096.bcl

import io.github.takenoko4096.json.JsonFile
import io.github.takenoko4096.json.JsonPath
import io.github.takenoko4096.json.JsonValueTypes
import java.io.PrintStream
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

@OptIn(ExperimentalPathApi::class)
fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))
    val file = JsonFile("src/test/resources/mojang-commands.json")
    val obj = file.readAsObject()
    val dataItems =  obj.getOrThrow(JsonPath.of("command_enums[{\"name\": \"EntityEvents\"}].values"), JsonValueTypes.ARRAY).typed(JsonValueTypes.OBJECT)
        .map { item ->
            val s = item.getOrThrow("value", JsonValueTypes.STRING).value

            if (s.first().isJavaIdentifierStart() && s.all { char -> char.isJavaIdentifierPart() }) {
                s.uppercase()
            }
            else {
                "`${s.uppercase()}`"
            }
        }
        .sortedBy { it }

    val sourceFile = Path("src/test/resources/classes/EntityEvents.kt")
    if (!sourceFile.parent.exists()) sourceFile.createParentDirectories()
    if (!sourceFile.exists()) sourceFile.createFile()

    sourceFile.writeText(
        """
package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class EntityEvents : StringRepresentable, CommandEnumerable {
${dataItems.joinToString(",\n    ", prefix = "    ", postfix = ";") { it }}

    override fun getString() = name.lowercase()

    override fun getNames() = setOf(getString())
}

""".trimIndent(),
        Charsets.UTF_8
    )
}
