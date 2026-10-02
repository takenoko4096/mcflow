package io.github.takenoko4096.bcl.brigadier.value.json

import io.github.takenoko4096.bcl.tokenizer.SourceSpan

class CommandJsonObject(
    val entries: List<Pair<CommandJsonValue<String>, CommandJsonValue<*>>>,
    sourceSpan: SourceSpan
) : CommandJsonValue<List<Pair<CommandJsonValue<String>, CommandJsonValue<*>>>>(entries, sourceSpan) {
    operator fun contains(key: String): Boolean {
        return entries.any { it.first.value == key }
    }

    fun getFirst(key: String): Pair<CommandJsonValue<String>, CommandJsonValue<*>>? {
        return entries.find { it.first.value == key }
    }

    fun getLast(key: String): Pair<CommandJsonValue<String>, CommandJsonValue<*>>? {
        return entries.findLast { it.first.value == key }
    }
}
