package io.github.takenoko4096.bcl

import io.github.takenoko4096.json.JsonValue
import io.github.takenoko4096.json.values.JsonArray
import io.github.takenoko4096.json.values.JsonNull
import io.github.takenoko4096.json.values.JsonObject
import io.github.takenoko4096.json.values.JsonString
import java.lang.reflect.Field

class JsonReflector(private vararg val targets: Class<*>) {
    private fun fields(clazz: Class<*>): Set<Field> {
        return if (clazz == Any::class.java) setOf()
        else clazz.declaredFields.filterNot { it.name == "Companion" }.toSet() + fields(clazz.superclass)
    }

    private fun primitive(obj: Any): JsonValue<*> {
        return when (obj) {
            is Enum<*> -> JsonString.valueOf(obj.name)
            is Boolean, is Number, is String -> JsonValue.valueOf(obj)
            is Iterable<*> -> JsonArray.valueOf(obj.map { extract(it) })
            is Array<*> -> JsonArray.valueOf(obj.map { extract(it) })
            is Map<*, *> -> {
                JsonObject.valueOf(obj.mapKeys { it.key.toString() }.mapValues { extract(it.value) })
            }
            is JsonValue<*> -> obj
            else -> extract(obj)
        }
    }

    private fun `object`(obj: Any): JsonObject {
        val map = mutableMapOf<String, JsonValue<*>>(
            "CLASS_NAME" to JsonString.valueOf(obj.javaClass.simpleName)
        )

        for (field in fields(obj.javaClass)) {
            if (field.trySetAccessible()) {
                val value = field.get(obj)

                map[field.name] = if (value == null) JsonNull.NULL
                else primitive(value)
            }
            else {
                throw IllegalStateException("JSON REFLECT FAILURE")
            }
        }

        return JsonObject.valueOf(map)
    }

    private fun extract(obj: Any?): JsonValue<*> {
        return if (obj == null) JsonNull.NULL
        else if (targets.any { target -> target.isInstance(obj) }) `object`(obj)
        else JsonString.valueOf(obj.toString())
    }

    fun reflect(value: Any?): JsonObject {
        val obj = extract(value)

        if (obj is JsonObject) return obj
        else throw IllegalArgumentException("不正な出力を得ました")
    }
}