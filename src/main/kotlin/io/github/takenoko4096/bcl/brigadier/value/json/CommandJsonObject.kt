package io.github.takenoko4096.bcl.brigadier.value.json

import io.github.takenoko4096.bcl.tokenizer.SourceSpan
import kotlin.reflect.KClass
import kotlin.reflect.cast

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

    fun <T : CommandJsonValue<*>> getWrappers(key: String, clazz: KClass<T>): List<T> {
        return entries.filter { it.first.value == key }.map { clazz.cast(it.second) }
    }

    fun getStrings(key: String): List<CommandJsonValue<String>> {
        return entries.filter { it.first.value == key }.map { it.second as CommandJsonValue<String> }
    }

    fun getAny(key: String): List<CommandJsonValue<*>> {
        return entries.filter { it.first.value == key }.map { it.second }
    }
}
