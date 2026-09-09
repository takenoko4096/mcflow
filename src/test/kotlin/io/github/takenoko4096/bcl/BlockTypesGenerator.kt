package io.github.takenoko4096.bcl

import io.github.takenoko4096.json.JsonFile
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

data class BlockDataItem(
    val name: String,
    val properties: List<String>
)

@OptIn(ExperimentalPathApi::class)
fun main() {
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))
    val file = JsonFile("src/test/resources/mojang-blocks.json")
    val obj = file.readAsObject()
    val dataItems =  obj.getOrThrow("data_items", JsonValueTypes.ARRAY).typed(JsonValueTypes.OBJECT)
        .map { item ->
            val blockId = item.getOrThrow("name", JsonValueTypes.STRING).value
            val properties = item.getOrThrow("properties", JsonValueTypes.ARRAY).typed(JsonValueTypes.OBJECT)
                .map { it.getOrThrow("name", JsonValueTypes.STRING).value }
                .map { "BlockProperties.${it.replace(':', '_').uppercase()}" }
            val path = blockId.split(':')[1]

            BlockDataItem(path.uppercase(), properties)
        }

    val sourceFile = Path("src/test/resources/classes/BlockType.kt")
    if (!sourceFile.parent.exists()) sourceFile.createParentDirectories()
    if (!sourceFile.exists()) sourceFile.createFile()

    sourceFile.writeText(
"""
package io.github.takenoko4096.bcl.brigadier.value.block

import io.github.takenoko4096.bcl.brigadier.value.NamespacedIdentifier

enum class BlockType(namespace: String = NamespacedIdentifier.DEFAULT_NAMESPACE, value: String? = null, private val properties: Set<BlockProperty<*>> = setOf()) {
${dataItems.joinToString(",\n    ", prefix = "    ", postfix = ";") { "${it.name}${
    if (it.properties.isEmpty()) "" else {
        it.properties.joinToString(", ", prefix = "(", postfix = ")")
    }
}" }}

    constructor(vararg properties: BlockProperty<*>) : this(properties = properties.toSet())

    val identifier: NamespacedIdentifier = NamespacedIdentifier(namespace, value ?: name.lowercase())

    operator fun contains(property: BlockProperty<*>): Boolean {
        return property in properties
    }

    companion object {
        val properties: Set<BlockProperty<*>> = entries.flatMap { it.properties }.toSet()
    }
}
""".trimIndent(),
        Charsets.UTF_8
    )
}
