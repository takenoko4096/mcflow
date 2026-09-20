package io.github.takenoko4096.bcl

import io.github.takenoko4096.json.JsonFile
import io.github.takenoko4096.json.JsonPath
import io.github.takenoko4096.json.JsonValueTypes
import java.io.PrintStream
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))
    val file = JsonFile("src/test/resources/mojang-commands.json")
    val obj = file.readAsObject()
    val dataItems =  obj.getOrThrow(JsonPath.of("command_enums[{\"name\": \"UnlockableRecipeValues\"}].values"), JsonValueTypes.ARRAY)
        .typed(JsonValueTypes.OBJECT)
        .map { '"' + it.getOrThrow("value", JsonValueTypes.STRING).value + '"' }

    val sourceFile = Path("src/test/resources/classes/Recipes.kt")
    if (!sourceFile.parent.exists()) sourceFile.createParentDirectories()
    if (!sourceFile.exists()) sourceFile.createFile()

    sourceFile.writeText(
        """
package io.github.takenoko4096.bcl.brigadier.value.misc

val RECIPES = arrayOf(
${dataItems.joinToString(",\n    ", prefix = "    ") { it }}
)
""".trimIndent(),
        Charsets.UTF_8
    )
}
