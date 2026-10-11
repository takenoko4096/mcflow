package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue
import io.github.takenoko4096.bcl.completer.ArgumentCompleter

object NumberSchema : CommandJsonSchema() {
    override fun validate(value: CommandJsonValue<*>, sourceCode: String, diagnostics: MutableList<BclDiagnostic>): Boolean {
        return value.value is Number
    }

    override fun completeJson(completer: ArgumentCompleter): JsonCompletionResult {
        return when (completer.completeNumber()) {
            is ArgumentCompleter.ShouldComplete -> JsonCompletionResult.Complete(emptyList())
            is ArgumentCompleter.ShouldContinue -> JsonCompletionResult.Continue
        }
    }
}
