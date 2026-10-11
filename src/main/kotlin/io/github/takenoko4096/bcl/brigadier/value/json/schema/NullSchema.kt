package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.completer.ArgumentCompleter
import org.eclipse.lsp4j.CompletionItemKind

object NullSchema : CommandJsonSchema() {
    override fun validate(value: CommandJsonValue<*>, sourceCode: String, diagnostics: MutableList<BclDiagnostic>): Boolean {
        return value.value == null
    }

    override fun completeJson(completer: ArgumentCompleter): JsonCompletionResult {
        return when (
            val result = completer.completeUnquotedString(
                listOf("null"),
                CompletionItemKind.Keyword
            )
        ) {
            is ArgumentCompleter.ShouldComplete ->
                JsonCompletionResult.Complete(result.items)

            is ArgumentCompleter.ShouldContinue ->
                JsonCompletionResult.Continue
        }
    }
}
