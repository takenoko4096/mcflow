package io.github.takenoko4096.bcl.brigadier.value.entity

import io.github.takenoko4096.bcl.brigadier.value.CommandEnumerable

enum class PermissionLevel : CommandEnumerable {
    VISITOR,
    MEMBER,
    OPERATOR;

    override fun getNames(): Set<String> {
        return setOf(name.lowercase())
    }
}
