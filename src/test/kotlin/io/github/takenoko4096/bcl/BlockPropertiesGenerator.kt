package io.github.takenoko4096.bcl

import io.github.takenoko4096.json.JsonFile
import io.github.takenoko4096.json.JsonValueTypes
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.writeText

fun String.toUpperCamel(): String {
    return split('_')
        .filter { it.isNotEmpty() }
        .joinToString("") { it.lowercase().replaceFirstChar(Char::uppercase) }
}

@OptIn(ExperimentalPathApi::class)
fun main() {
    val file = JsonFile("src/test/resources/mojang-blocks.json")
    val obj = file.readAsObject()
    val properties =  obj.getOrThrow("block_properties", JsonValueTypes.ARRAY).typed(JsonValueTypes.OBJECT)

    val dir = Path("src/test/resources/classes/properties")
    if (!dir.exists()) dir.createDirectories()

    for (property in properties) {
        val name = property.getOrThrow("name", JsonValueTypes.STRING).value
        val values = property.getOrThrow("values", JsonValueTypes.ARRAY).typed(JsonValueTypes.OBJECT)
        when(property.getOrThrow("type", JsonValueTypes.STRING).value) {
            "string" -> {
                val entries = values.map { it.getOrThrow("value", JsonValueTypes.STRING).value }
                val className = name.replace(':', '_').toUpperCamel()
                val sourceFile = dir.resolve("${className}.kt").createFile()
                sourceFile.writeText(
"""
package io.github.takenoko4096.bcl.brigadier.value.block.properties

import io.github.takenoko4096.bcl.brigadier.value.StringRepresentable

enum class $className : StringRepresentable {
${entries.joinToString(",\n    ", prefix = "    ", postfix = ";") { it.uppercase() }}

    override fun getString() = name.lowercase()
}
""".trimIndent(),
                    Charsets.UTF_8
                )

                println("val ${name.uppercase().replace(':', '_')} = BlockProperty.enum<$className>(\"$name\")")
            }
            "int" -> {
                val entries = values.map { it.getOrThrow("value", JsonValueTypes.NUMBER).value }
                val min = entries.minBy { it.toInt() }
                val max = entries.maxBy { it.toInt() }
                println("val ${name.uppercase().replace(':', '_')} = BlockProperty.integer(\"$name\", $min..$max)")
            }
            "bool" -> {
                println("val ${name.uppercase().replace(':', '_')} = BlockProperty.boolean(\"$name\")")
            }
            else -> throw IllegalStateException("NEVER HAPPENS")
        }
    }
}
