package io.github.takenoko4096.bcl

import io.github.takenoko4096.json.JsonFile
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
    val commands =  obj.getOrThrow("commands", JsonValueTypes.ARRAY).typed(JsonValueTypes.OBJECT)
        .map { item ->
            val name = item.getOrThrow("name", JsonValueTypes.STRING).value
            val aliases = item.getOrThrow("aliases", JsonValueTypes.ARRAY)
                .typed(JsonValueTypes.OBJECT)
                .map { it.getOrThrow("name", JsonValueTypes.STRING).value }
            name to aliases
        }

    val sourceFile = Path("src/test/resources/classes/CommandName.kt")
    if (!sourceFile.parent.exists()) sourceFile.createParentDirectories()
    if (!sourceFile.exists()) sourceFile.createFile()

    sourceFile.writeText(
        """
package io.github.takenoko4096.bcl.brigadier.value.misc

enum class CommandName(private val aliases: Set<String>) : AliasRepresentable {
${
    commands.joinToString(",\n    ", prefix = "    ", postfix = ";") { (name, aliases) ->
        val entryName = if (name.contains('-')) "`${name.uppercase()}`" else name.uppercase()
        if (aliases.isEmpty()) entryName
        else "$entryName(${aliases.joinToString(", ") { "\"$it\"" }})"
    }
}

    constructor(vararg aliases: String) : this(aliases.toSet())
    
    override fun getAliases() = setOf(name.lowercase()) + aliases
}
""".trimIndent(),
        Charsets.UTF_8
    )
}
