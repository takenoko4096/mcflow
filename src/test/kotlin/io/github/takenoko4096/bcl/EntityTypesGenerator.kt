package io.github.takenoko4096.bcl

import io.github.takenoko4096.json.JsonFile
import io.github.takenoko4096.json.JsonPath
import io.github.takenoko4096.json.JsonValueTypes
import java.io.PrintStream
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.createParentDirectories
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.writeText

@OptIn(ExperimentalPathApi::class)
fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))
    val file = JsonFile("src/test/resources/mojang-commands.json")
    val obj = file.readAsObject()
    val dataItems =  obj.getOrThrow(JsonPath.of("command_enums[{\"name\": \"EntityType\"}].values"), JsonValueTypes.ARRAY).typed(JsonValueTypes.OBJECT)
        .map { item ->
            item.getOrThrow("value", JsonValueTypes.STRING).value
        }
        .filterNot {
            it.startsWith("minecraft:")
        }
        .sortedBy { it }
        .toMutableList()

    dataItems.add("player")

    val sourceFile = Path("src/test/resources/classes/EntityType.kt")
    if (!sourceFile.parent.exists()) sourceFile.createParentDirectories()
    if (!sourceFile.exists()) sourceFile.createFile()

    sourceFile.writeText(
        """
package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

enum class EntityType(namespace: String = NamespacedIdentifier.DEFAULT_NAMESPACE, value: String? = null) {
${dataItems.joinToString(",\n    ", prefix = "    ", postfix = ";") {
    if (it.first().isJavaIdentifierStart() && it.all { char -> char.isJavaIdentifierPart() }) {
        it.uppercase()
    }
    else {
        val (namespace, id) = it.split(':')
        "`${it.uppercase()}`(\"$namespace\", \"$id\")"
    }
}}

    val identifier = NamespacedIdentifier(namespace, value ?: name.lowercase())

    companion object {
        fun get(identifier: NamespacedIdentifier): EntityType? {
            return entries.find { it.identifier == identifier }
        }
    }
}

""".trimIndent(),
        Charsets.UTF_8
    )
}
