package io.github.takenoko4096.bcl.brigadier.value.json.schema

import io.github.takenoko4096.bcl.brigadier.BclDiagnostic
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonArray
import io.github.takenoko4096.bcl.brigadier.value.json.CommandJsonValue

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
}
