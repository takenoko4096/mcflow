package io.github.takenoko4096.bcl

import io.github.takenoko4096.json.JsonFile
import io.github.takenoko4096.json.JsonValueTypes
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeText

@OptIn(ExperimentalPathApi::class)
fun main() {
    val file = JsonFile("src/test/resources/mojang-commands.json")
    val obj = file.readAsObject()
    val properties =  obj.getOrThrow("command_enums", JsonValueTypes.ARRAY).typed(JsonValueTypes.OBJECT)

    val dir = Path("src/test/resources/classes")
    if (!dir.exists()) dir.createDirectories()

    for (property in properties) {
        var name = property.getOrThrow("name", JsonValueTypes.STRING).value
        if (name.first().isLowerCase()) name = name.toUpperCamel()

        val values = property.getOrThrow("values", JsonValueTypes.ARRAY).typed(JsonValueTypes.OBJECT)
            .map {
                val s = it.getOrThrow("value", JsonValueTypes.STRING).value

                if (!s.first().isJavaIdentifierStart() || s.any { !it.isJavaIdentifierPart() }) {
                    "`$s`"
                }
                else s
            }
            .distinct()

        val className = if (name.first().isJavaIdentifierStart()) name else "`$name`"

        if (values.size == 1) continue

        val sourceFile = dir.resolve("$name.kt")
        sourceFile.writeText(
"""
package io.github.takenoko4096.bcl.brigadier.value.misc

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable
import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class $className : StringRepresentable, CommandEnumerable {
${values.joinToString(",\n    ", prefix = "    ", postfix = ";") { it.uppercase() }}

    override fun getString() = name.lowercase()

    override fun getNames() = setOf(getString())
}

""".trimIndent()
        )
    }
}
