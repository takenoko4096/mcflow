package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import org.eclipse.lsp4j.CompletionItemKind

class StringSchema(
    private val customCompletion: ((ArgumentCompleter) -> JsonCompletionResult) = { completer ->
        when (val result = completer.completeQuotedString(emptyList(), CompletionItemKind.Value)) {
            is ArgumentCompleter.ShouldComplete -> JsonCompletionResult.Complete(result.items)
            is ArgumentCompleter.ShouldContinue -> JsonCompletionResult.Continue
        }
    }
) : CommandJsonSchema() {
    override fun validate(value: CommandJsonValue<*>, sourceCode: String, diagnostics: MutableList<BclDiagnostic>): Boolean {
        return value.value is String
    }

    override fun completeJson(completer: ArgumentCompleter): JsonCompletionResult {
        return customCompletion(completer)
    }
}
