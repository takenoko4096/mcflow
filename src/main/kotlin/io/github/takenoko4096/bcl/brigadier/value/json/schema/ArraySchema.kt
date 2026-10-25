package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.completer.BclCompleter
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.InsertTextFormat

class ArraySchema(
    val elementSchemas: Set<CommandJsonSchema>
) : CommandJsonSchema() {

    override fun validate(
        value: CommandJsonValue<*>,
        sourceCode: String,
        diagnostics: MutableList<BclDiagnostic>
    ): Boolean {
        if (value !is CommandJsonArray) {
            diagnostics.add(
                BclDiagnostic(
                    "ここでは配列が期待されています",
                    sourceCode,
                    value.sourceSpan
                )
            )
            return false
        }

        var result = true

        for (element in value.list) {
            var elementResult = false
            val dummyDiagnostics = mutableListOf<BclDiagnostic>()

            for (schema in elementSchemas) {
                if (schema.validate(element, sourceCode, dummyDiagnostics)) {
                    elementResult = true
                }
            }

            if (!elementResult) {
                diagnostics.addAll(dummyDiagnostics)
                result = false
            }
        }

        return result
    }

    override fun completeJson(
        completer: BclCompleter
    ): JsonCompletionResult {
        val start = completer.checkpoint()

        val openResult = completer.completeChar(
            listOf('['),
            CompletionItemKind.Keyword
        )

        when (openResult) {
            is BclCompleter.ShouldComplete -> {
                if (openResult.items.isEmpty()) {
                    return JsonCompletionResult.Invalid
                }

                return JsonCompletionResult.Complete(
                    listOf(
                        CompletionItem("[]").apply {
                            insertTextFormat = InsertTextFormat.Snippet
                            insertText = "[$0]"
                        }
                    )
                )
            }

            is BclCompleter.ShouldContinue -> {
                // 開き括弧を消費したので続行
            }
        }

        completer.skipWhitespaceAndComments()

        while (true) {
            /*
             * [] または [ ... ]
             */
            if (!completer.isOver() && completer.peek() == ']') {
                completer.next()
                return JsonCompletionResult.Continue
            }

            when (
                val result = completeSchemaUnion(
                    completer,
                    elementSchemas
                )
            ) {
                is JsonCompletionResult.Complete -> {
                    return result
                }

                JsonCompletionResult.Continue -> {
                    // 要素が完成したのでseparatorへ進む
                }

                JsonCompletionResult.Invalid -> {
                    return JsonCompletionResult.Invalid
                }
            }

            completer.skipWhitespaceAndComments()

            when (
                val separatorResult = completer.completeChar(
                    listOf(',', ']'),
                    CompletionItemKind.Keyword
                )
            ) {
                is BclCompleter.ShouldComplete -> {
                    if (separatorResult.items.isEmpty()) {
                        return JsonCompletionResult.Invalid
                    }

                    return JsonCompletionResult.Complete(
                        separatorResult.items
                    )
                }

                is BclCompleter.ShouldContinue -> {
                    if (separatorResult.input == "]") {
                        return JsonCompletionResult.Continue
                    }
                }
            }

            completer.skipWhitespaceAndComments()
        }
    }
}