package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.BclDsl
import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
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

        val number = NumberSchema

        val boolean = BooleanSchema

        init {
            callback()
        }

        fun createString(completer: ((ArgumentCompleter) -> JsonCompletionResult)? = null) = if (completer == null) StringSchema() else StringSchema(completer)

        fun append(schema: CommandJsonSchema) {
            schemas.add(schema)
        }

        fun appendObject(callback: Builder.() -> Unit) {
            schemas.add(Builder.build(callback))
        }

        fun appendArray(vararg schemas: CommandJsonSchema) {
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
        data object Invalid: JsonCompletionResult()
    }

    abstract fun completeJson(completer: ArgumentCompleter): JsonCompletionResult

    companion object {
        fun completeSchemaUnion(
            completer: ArgumentCompleter,
            schemas: Set<CommandJsonSchema>
        ): JsonCompletionResult {
            if (schemas.isEmpty()) {
                return JsonCompletionResult.Invalid
            }

            data class Candidate(
                val result: JsonCompletionResult,
                val end: ArgumentCompleter.Checkpoint
            )

            val start = completer.checkpoint()
            val candidates = mutableListOf<Candidate>()

            for (schema in schemas) {
                completer.restore(start)

                val result = schema.completeJson(completer)

                if (result != JsonCompletionResult.Invalid) {
                    candidates += Candidate(
                        result = result,
                        end = completer.checkpoint()
                    )
                }
            }

            if (candidates.isEmpty()) {
                completer.restore(start)
                return JsonCompletionResult.Invalid
            }

            /*
             * 最も入力を消費できたschemaを優先する。
             */
            val maxCursor = candidates.maxOf { it.end.cursor }

            val best = candidates.filter {
                it.end.cursor == maxCursor
            }

            /*
             * 同じ位置まで解析できたschemaが複数ある場合は、
             * CompletionItemを統合する。
             */
            val items = best
                .flatMap { candidate ->
                    when (val result = candidate.result) {
                        is JsonCompletionResult.Complete -> result.items
                        JsonCompletionResult.Continue,
                        JsonCompletionResult.Invalid -> emptyList()
                    }
                }
                .distinctBy { it.label }

            /*
             * 採用した解析結果の位置へcursorを戻す。
             */
            completer.restore(best.first().end)

            return when {
                items.isNotEmpty() -> {
                    JsonCompletionResult.Complete(items)
                }

                best.all { it.result is JsonCompletionResult.Continue } -> {
                    JsonCompletionResult.Continue
                }

                else -> {
                    /*
                     * 有効なschemaは存在するが、
                     * 現在提示できるCompletionItemはない。
                     */
                    JsonCompletionResult.Complete(emptyList())
                }
            }
        }
    }
}
