package io.github.takenoko4096.bcl.brigadier.value.json.schema

data class KeySchema(
    val isOptional: Boolean,
    val isNullable: Boolean,
    val duplication: KeyDuplicationRule,
    val schemas: Set<CommandJsonSchema>
)
