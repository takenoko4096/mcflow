package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.InsertTextFormat

class ObjectSchema(val keys: Map<String, KeySchema>) : CommandJsonSchema() {
    override fun validate(value: CommandJsonValue<*>, sourceCode: String, diagnostics: MutableList<BclDiagnostic>): Boolean {
        if (value !is CommandJsonObject) {
            diagnostics.add(BclDiagnostic("ここではオブジェクトが期待されています", sourceCode, value.sourceSpan))
            return false
        }

        var result = true

        for ((name, keySchema) in keys) {
            val names = keySchema.aliases + name

            if (names.none { it in value } && !keySchema.isOptional) {
                diagnostics.add(BclDiagnostic("キー '$name' は必須です", sourceCode, value.sourceSpan))
                result = false
            }

            for ((key, child) in value.entries.filter { it.first.value in names }) {
                if (!keySchema.isNullable && child.value == null) {
                    diagnostics.add(BclDiagnostic("キー '${key.value}' は null を受け入れません", sourceCode, child.sourceSpan))
                    result = false
                }

                if (!keySchema.duplicatable && value.entries.count { it.first.value in names } > 1) {
                    diagnostics.add(BclDiagnostic("キー '${key.value}' が重複しています", sourceCode, key.sourceSpan))
                    result = false
                }

                var keyResult = false
                val dummyDiagnostics = mutableListOf<BclDiagnostic>()
                for (schema in keySchema.schemas) {
                    if (schema.validate(child, sourceCode, dummyDiagnostics)) {
                        keyResult = true
                    }
                }

                if (!keyResult) {
                    diagnostics.addAll(dummyDiagnostics)
                    result = false
                }
            }
        }

        return result
    }

    override fun completeJson(completer: ArgumentCompleter): JsonCompletionResult {
        val openResult = completer.completeChar(
            listOf('{'),
            CompletionItemKind.Keyword
        )

        if (openResult is ArgumentCompleter.ShouldComplete) {
            return JsonCompletionResult.Complete(
                listOf(
                    CompletionItem("{}").apply {
                        insertTextFormat = InsertTextFormat.Snippet
                        insertText = "{$0}"
                    }
                )
            )
        }
        completer.skipWhitespaceAndComments()

        val usedKeys = mutableSetOf<String>()

        while (true) {
            if (!completer.isOver() && completer.peek() == '}') {
                completer.next()
                return JsonCompletionResult.Continue
            }

            val candidates = keys.filter { (name, schema) -> schema.duplicatable || name !in usedKeys }

            val keyResult = completer.completeQuotedString(
                candidates.keys.toList(),
                CompletionItemKind.Property
            )

            if (keyResult is ArgumentCompleter.ShouldComplete) {
                return JsonCompletionResult.Complete(
                    keyResult.items
                )
            }
            completer.skipWhitespaceAndComments()

            keyResult as ArgumentCompleter.ShouldContinue

            val key = keyResult.input

            val keyEntry = keys.entries.firstOrNull { (name, schema) ->
                name == key || key in schema.aliases
            } ?: return JsonCompletionResult.Continue

            val (canonicalKey, keySchema) = keyEntry
            usedKeys += canonicalKey

            val colonResult = completer.completeChar(
                listOf(KeyChar.COLON),
                CompletionItemKind.Keyword
            )

            if (colonResult is ArgumentCompleter.ShouldComplete) {
                return JsonCompletionResult.Complete(colonResult.items)
            }
            completer.skipWhitespaceAndComments()

            when (
                val result = completeSchemaUnion(
                    completer,
                    buildSet {
                        addAll(keySchema.schemas)
                        if (keySchema.isNullable) {
                            add(NullSchema)
                        }
                    }
                )
            ) {
                is JsonCompletionResult.Complete -> return result
                JsonCompletionResult.Continue -> {}
            }
            completer.skipWhitespaceAndComments()

            val separatorResult = completer.completeChar(
                listOf(',', '}'),
                CompletionItemKind.Keyword
            )

            if (separatorResult is ArgumentCompleter.ShouldComplete) {
                return JsonCompletionResult.Complete(
                    separatorResult.items.filter { item ->
                        item.label != "}" || canClose(usedKeys)
                    }
                )
            }
            completer.skipWhitespaceAndComments()

            separatorResult as ArgumentCompleter.ShouldContinue

            if (separatorResult.input == "}") {
                return JsonCompletionResult.Continue
            }
            completer.skipWhitespaceAndComments()
        }
    }

    private fun canClose(usedKeys: Set<String>): Boolean {
        return keys.all { (name, schema) ->
            schema.isOptional ||
                schema.duplicatable ||
                name in usedKeys
        }
    }
}
