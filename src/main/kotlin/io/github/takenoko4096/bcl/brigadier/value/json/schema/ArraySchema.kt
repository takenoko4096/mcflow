package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.InsertTextFormat

class ArraySchema(val elementSchemas: Set<CommandJsonSchema>) : CommandJsonSchema() {
    override fun validate(value: CommandJsonValue<*>, sourceCode: String, diagnostics: MutableList<BclDiagnostic>): Boolean {
        if (value !is CommandJsonArray) {
            diagnostics.add(BclDiagnostic("ここでは配列が期待されています", sourceCode, value.sourceSpan))
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

    override fun completeJson(completer: ArgumentCompleter): JsonCompletionResult {
        val openResult = completer.completeChar(
            listOf('['),
            CompletionItemKind.Keyword
        )

        if (openResult is ArgumentCompleter.ShouldComplete) {
            return JsonCompletionResult.Complete(
                listOf(
                    CompletionItem("[]").apply {
                        insertTextFormat = InsertTextFormat.Snippet
                        insertText = "[$0]"
                    }
                )
            )
        }
        completer.skipWhitespaceAndComments()

        while (true) {
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
                is JsonCompletionResult.Complete -> return result
                JsonCompletionResult.Continue -> {}
            }
            completer.skipWhitespaceAndComments()

            val separatorResult = completer.completeChar(
                listOf(',', ']'),
                CompletionItemKind.Keyword
            )

            if (separatorResult is ArgumentCompleter.ShouldComplete) {
                return JsonCompletionResult.Complete(separatorResult.items)
            }
            completer.skipWhitespaceAndComments()

            separatorResult as ArgumentCompleter.ShouldContinue

            if (separatorResult.input == "]") {
                return JsonCompletionResult.Continue
            }
        }
    }
}
