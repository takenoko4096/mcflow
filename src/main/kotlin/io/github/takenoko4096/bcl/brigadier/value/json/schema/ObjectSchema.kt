package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonObject
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import io.github.takenoko4096.bcl.tokenizer.KeyChar
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.InsertTextFormat

class ObjectSchema(
    val keys: Map<String, KeySchema>
) : CommandJsonSchema() {
    override fun validate(value: CommandJsonValue<*>, sourceCode: String, diagnostics: MutableList<BclDiagnostic>): Boolean {
        if (value !is CommandJsonObject) {
            diagnostics.add(
                BclDiagnostic(
                    "ここではオブジェクトが期待されています",
                    sourceCode,
                    value.sourceSpan
                )
            )
            return false
        }

        var result = true

        for ((name, keySchema) in keys) {
            val names = keySchema.aliases + name

            if (names.none { it in value } && !keySchema.isOptional) {
                diagnostics.add(
                    BclDiagnostic(
                        "キー '$name' は必須です",
                        sourceCode,
                        value.sourceSpan
                    )
                )
                result = false
            }

            for ((key, child) in value.entries.filter { it.first.value in names }) {
                if (!keySchema.isNullable && child.value == null) {
                    diagnostics.add(
                        BclDiagnostic(
                            "キー '${key.value}' は null を受け入れません",
                            sourceCode,
                            child.sourceSpan
                        )
                    )
                    result = false
                }

                if (!keySchema.duplicatable && value.entries.count { it.first.value in names } > 1) {
                    diagnostics.add(
                        BclDiagnostic(
                            "キー '${key.value}' が重複しています",
                            sourceCode,
                            key.sourceSpan
                        )
                    )
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

    override fun completeJson(
        completer: ArgumentCompleter
    ): JsonCompletionResult {
        val start = completer.checkpoint()

        val openResult = completer.completeChar(
            listOf('{'),
            CompletionItemKind.Keyword
        )

        when (openResult) {
            is ArgumentCompleter.ShouldComplete -> {
                if (openResult.items.isEmpty()) {
                    completer.restore(start)
                    return JsonCompletionResult.Invalid
                }

                return JsonCompletionResult.Complete(
                    listOf(
                        CompletionItem("{}").apply {
                            insertTextFormat = InsertTextFormat.Snippet
                            insertText = "{$0}"
                        }
                    )
                )
            }

            is ArgumentCompleter.ShouldContinue -> {
                // 開き括弧を消費したので続行
            }
        }

        completer.skipWhitespaceAndComments()

        /*
         * canonical key nameを格納する。
         *
         * aliasで入力された場合もcanonical nameを記録する。
         */
        val usedKeys = mutableSetOf<String>()

        while (true) {
            /*
             * 空のオブジェクト、またはオブジェクトの終了。
             *
             * 必須keyが残っている場合は } を許さない。
             */
            if (!completer.isOver() && completer.peek() == '}') {
                if (!canClose(usedKeys)) {
                    return JsonCompletionResult.Invalid
                }

                completer.next()
                return JsonCompletionResult.Continue
            }

            /*
             * 現在入力可能なkeyを求める。
             *
             * duplicatableなkeyは何度でも候補に出せる。
             */
            val candidates = keys
                .filter { (name, schema) ->
                    schema.duplicatable || name !in usedKeys
                }
                .keys
                .toList()

            val keyResult = completer.completeQuotedString(
                candidates,
                CompletionItemKind.Property
            )

            when (keyResult) {
                is ArgumentCompleter.ShouldComplete -> {
                    return if (keyResult.items.isEmpty()) {
                        JsonCompletionResult.Invalid
                    } else {
                        JsonCompletionResult.Complete(
                            keyResult.items
                        )
                    }
                }

                is ArgumentCompleter.ShouldContinue -> {
                    // keyを読み終えた
                }
            }

            completer.skipWhitespaceAndComments()

            val key = keyResult.input

            /*
             * canonical nameまたはaliasからschemaを探す。
             */
            val keyEntry = keys.entries.firstOrNull { (name, schema) ->
                name == key || key in schema.aliases
            } ?: return JsonCompletionResult.Invalid

            val (canonicalKey, keySchema) = keyEntry

            usedKeys += canonicalKey

            /*
             * :
             */
            when (
                val colonResult = completer.completeChar(
                    listOf(KeyChar.COLON),
                    CompletionItemKind.Keyword
                )
            ) {
                is ArgumentCompleter.ShouldComplete -> {
                    if (colonResult.items.isEmpty()) {
                        return JsonCompletionResult.Invalid
                    }

                    return JsonCompletionResult.Complete(
                        colonResult.items
                    )
                }

                is ArgumentCompleter.ShouldContinue -> {
                    // : を消費した
                }
            }

            completer.skipWhitespaceAndComments()

            /*
             * value
             */
            val valueSchemas = buildSet {
                addAll(keySchema.schemas)

                if (keySchema.isNullable) {
                    add(NullSchema)
                }
            }

            when (
                val result = completeSchemaUnion(
                    completer,
                    valueSchemas
                )
            ) {
                is JsonCompletionResult.Complete -> {
                    return result
                }

                JsonCompletionResult.Continue -> {
                    // valueが完成したのでseparatorへ
                }

                JsonCompletionResult.Invalid -> {
                    return JsonCompletionResult.Invalid
                }
            }

            completer.skipWhitespaceAndComments()

            /*
             * , または }
             */
            when (
                val separatorResult = completer.completeChar(
                    listOf(',', '}'),
                    CompletionItemKind.Keyword
                )
            ) {
                is ArgumentCompleter.ShouldComplete -> {
                    val items = separatorResult.items.filter { item ->
                        item.label != "}" || canClose(usedKeys)
                    }

                    return if (items.isEmpty()) {
                        JsonCompletionResult.Invalid
                    } else {
                        JsonCompletionResult.Complete(items)
                    }
                }

                is ArgumentCompleter.ShouldContinue -> {
                    if (separatorResult.input == "}") {
                        if (!canClose(usedKeys)) {
                            return JsonCompletionResult.Invalid
                        }

                        return JsonCompletionResult.Continue
                    }
                }
            }

            completer.skipWhitespaceAndComments()
        }
    }

    private fun canClose(
        usedKeys: Set<String>
    ): Boolean {
        return keys.all { (name, schema) ->
            schema.isOptional || name in usedKeys
        }
    }
}