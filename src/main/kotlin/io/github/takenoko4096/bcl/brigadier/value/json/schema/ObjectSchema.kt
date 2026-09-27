package io.github.takenoko4096.bcl.brigadier.value.json.schema

class ObjectSchema(
    val keys: Map<String, KeySchema>
) : CommandJsonSchema()
