package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.completer.BclCompleter
import org.eclipse.lsp4j.CompletionItemKind

class StringSchema(
    private val customCompletion: ((BclCompleter) -> JsonCompletionResult) = { completer ->
        when (val result = completer.completeQuotedString(emptyList(), CompletionItemKind.Value)) {
            is BclCompleter.ShouldComplete -> JsonCompletionResult.Complete(result.items)
            is BclCompleter.ShouldContinue -> JsonCompletionResult.Continue
        }
    }
) : CommandJsonSchema() {
    override fun validate(value: CommandJsonValue<*>, sourceCode: String, diagnostics: MutableList<BclDiagnostic>): Boolean {
        return value.value is String
    }

    override fun completeJson(completer: BclCompleter): JsonCompletionResult {
        return customCompletion(completer)
    }
}
