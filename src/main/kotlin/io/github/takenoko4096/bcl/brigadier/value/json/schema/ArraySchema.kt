package io.github.takenoko4096.bcl.brigadier.value.json.schema

class ArraySchema(
    val elementSchemas: Set<CommandJsonSchema>
) : CommandJsonSchema()
