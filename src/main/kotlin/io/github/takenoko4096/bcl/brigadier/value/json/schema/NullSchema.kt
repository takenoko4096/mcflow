package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.completer.BclCompleter
import org.eclipse.lsp4j.CompletionItemKind

object NullSchema : CommandJsonSchema() {

    override fun validate(value: CommandJsonValue<*>, sourceCode: String, diagnostics: MutableList<BclDiagnostic>): Boolean {
        return value.value == null
    }

    override fun completeJson(completer: BclCompleter): JsonCompletionResult {
        return when (val result = completer.completeUnquotedString(listOf("null"), CompletionItemKind.Keyword)) {
            is BclCompleter.ShouldComplete -> {
                if (result.items.isEmpty()) JsonCompletionResult.Invalid
                else JsonCompletionResult.Complete(result.items)
            }
            is BclCompleter.ShouldContinue -> JsonCompletionResult.Continue
        }
    }
}
