package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.BclDsl
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticToken
import kotlin.collections.set

sealed class CommandJsonSchema {
    abstract fun validate(value: CommandJsonValue<*>, sourceCode: String, diagnostics: MutableList<BclDiagnostic>): Boolean

    @BclDsl
    class Builder private constructor(callback: Builder.() -> Unit) {
        private val keys = mutableMapOf<String, KeySchema>()

        init {
            callback()
        }

        fun property(name: String, callback: PropertyBuilder.() -> Unit) = PropertyBuilder.set(keys, name, callback)

        private fun build() = ObjectSchema(keys)

        companion object {
            fun build(callback: Builder.() -> Unit) = Builder(callback).build()
        }
    }

    @BclDsl
    class PropertyBuilder private constructor(val name: String, callback: PropertyBuilder.() -> Unit) {
        var optional: Boolean = false

        var nullable: Boolean = false

        var duplicatable: Boolean = false

        val aliases = mutableSetOf<String>()

        private val schemas = mutableSetOf<CommandJsonSchema>()

        val string = StringSchema

        val number = NumberSchema

        val boolean = BooleanSchema

        init {
            callback()
        }

        fun string() {
            schemas.add(string)
        }

        fun number() {
            schemas.add(number)
        }

        fun boolean() {
            schemas.add(boolean)
        }

        fun `object`(callback: Builder.() -> Unit) {
            schemas.add(Builder.build(callback))
        }

        fun array(vararg schemas: CommandJsonSchema) {
            this.schemas.add(ArraySchema(schemas.toSet()))
        }

        private fun set(keys: MutableMap<String, KeySchema>) {
            if (name in keys) {
                throw IllegalArgumentException("json スキーマのキー '$name' が重複しています")
            }

            if (schemas.isEmpty()) {
                throw IllegalStateException("json スキーマのキー '$name' の値の制約が設定されていません")
            }

            keys[name] = KeySchema(optional, nullable, duplicatable, aliases, schemas)
        }

        companion object {
            fun set(keys: MutableMap<String, KeySchema>, name: String, callback: PropertyBuilder.() -> Unit) = PropertyBuilder(name, callback).set(keys)
        }
    }
}
