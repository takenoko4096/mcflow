package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.BclDsl
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import io.github.takenoko4096.bcl.reader.TokenSeqReader
import io.github.takenoko4096.bcl.server.BclSemanticToken
import org.eclipse.lsp4j.CompletionItem
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

    sealed class JsonCompletionResult {
        data class Complete(val items: List<CompletionItem>) : JsonCompletionResult()
        data object Continue : JsonCompletionResult()
    }

    abstract fun completeJson(completer: ArgumentCompleter): JsonCompletionResult

    companion object {
        fun completeSchemaUnion(completer: ArgumentCompleter, schemas: Set<CommandJsonSchema>): JsonCompletionResult {
            if (schemas.isEmpty()) {
                return JsonCompletionResult.Continue
            }

            /*
             * 入力がまだ存在しない場合、各schemaが出せる候補を全部集める。
             *
             * ただし ObjectSchema / ArraySchema のように
             * '{' / '[' を消費するschemaについては、ここで直接
             * completeJson() を呼んでよい。
             */
            if (completer.isOver()) {
                val items = schemas.flatMap { schema ->
                    when (val result = schema.completeJson(completer)) {
                        is JsonCompletionResult.Complete -> result.items
                        JsonCompletionResult.Continue -> emptyList()
                    }
                }
                completer.skipWhitespaceAndComments()

                return JsonCompletionResult.Complete(
                    items.distinctBy { it.label }
                )
            }

            /*
             * 先頭文字によって候補schemaをある程度絞る。
             */
            val first = completer.peek()

            val matching = schemas.filter { schema ->
                when (schema) {
                    is ObjectSchema -> first == '{'
                    is ArraySchema -> first == '['
                    StringSchema -> first == '"'
                    BooleanSchema -> first == 't' || first == 'f'
                    NumberSchema -> first == '-' || first.isDigit()
                    NullSchema -> first == 'n'
                }
            }

            if (matching.isEmpty()) {
                return JsonCompletionResult.Continue
            }

            /*
             * 現状、最初に適合したschemaを解析する。
             *
             * BooleanSchemaとNullSchemaなどは入力文字によって一意に
             * 決まるので問題ない。
             */
            return matching.first().completeJson(completer)
        }
    }
}
