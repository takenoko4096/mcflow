package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.value.CommandJsonValue
import io.github.takenoko4096.bcl.completer.BclCompleter

object NumberSchema : CommandJsonSchema() {
    override fun validate(value: CommandJsonValue<*>, sourceCode: String, diagnostics: MutableList<BclDiagnostic>): Boolean {
        return value.value is Number
    }

    override fun complete(completer: BclCompleter): JsonCompletionResult {
        return completer.completeJsonNumber()
    }
}
